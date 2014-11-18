package controllers

import com.google.inject.Inject
import models.{CaptureCertificateDetailsModel, CaptureCertificateDetailsViewModel, CaptureCertificateDetailsFormModel}
import models.{VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel}
import org.joda.time.format.DateTimeFormat
import play.api.data.{FormError, Form => PlayForm}
import play.api.libs.json.Writes
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{CacheKey, ClientSideSessionFactory}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import utils.helpers.Config
import views.vrm_assign.Payment._
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.CaptureCertificateDetails._
import views.vrm_assign.VehicleLookup._
import audit.{AuditMessage, AuditService}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import play.api.mvc.Result
import webserviceclients.vrmretentioneligibility.{VrmAssignEligibilityService, VrmAssignEligibilityRequest}
import org.joda.time.{Period, DateTime}
import scala.util.Failure
import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext.Implicits.global


final class CaptureCertificateDetails @Inject()(val bruteForceService: BruteForcePreventionService,
                                                eligibilityService: VrmAssignEligibilityService,
                                                auditService: AuditService, dateService: DateService)
                                               (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                config: Config) extends Controller {

  type Form = CaptureCertificateDetailsFormModel

  private[controllers] val form = PlayForm(
    CaptureCertificateDetailsFormModel.Form.Mapping
  )

  def present = Action {
    implicit request =>
      request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
        case Some(vehicleAndKeeperDetails) =>
          val viewModel = CaptureCertificateDetailsViewModel(vehicleAndKeeperDetails)
          Ok(views.html.vrm_assign.capture_certificate_details(form.fill(), viewModel))
        case _ => Redirect(routes.VehicleLookup.present())
      }
  }

  def submit = Action.async {
    implicit request =>
      form.bindFromRequest.fold(
        invalidForm => {
          request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
            case Some(vehicleAndKeeperDetails) =>
              val captureCertificateDetailsViewModel = CaptureCertificateDetailsViewModel(vehicleAndKeeperDetails)
              Future.successful {
                BadRequest(views.html.vrm_assign.capture_certificate_details(formWithReplacedErrors(invalidForm),
                  captureCertificateDetailsViewModel))
              }
            case _ =>
              Future.successful {
                Redirect(routes.MicroServiceError.present())
              } // TODO is this correct
          }
        },
        validForm => {
          bruteForceAndLookup(
            validForm.prVrm,
            validForm.referenceNumber,
            validForm)
        }
      )
  }


  def bruteForceAndLookup(prVrm: String, referenceNumber: String, form: Form)
                         (implicit request: Request[_], toJson: Writes[Form], cacheKey: CacheKey[Form]): Future[Result] =
    bruteForceService.isVrmLookupPermitted(prVrm).flatMap { bruteForcePreventionModel =>
      val resultFuture = if (bruteForcePreventionModel.permitted) {
        // TODO use a match
        val vehicleAndKeeperLookupFormModel = request.cookies.getModel[VehicleAndKeeperLookupFormModel].get
        val vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel].get
        val transactionId = request.cookies.getString(TransactionIdCacheKey).get
        checkVrmEligibility(form, vehicleAndKeeperLookupFormModel, vehicleAndKeeperDetailsModel, transactionId)
      }
      else Future.successful {
        val anonRegistrationNumber = LogFormats.anonymize(prVrm)
        Logger.warn(s"BruteForceService locked out vrm: $anonRegistrationNumber")
        Redirect(routes.VrmLocked.present())
      }

      resultFuture.map { result =>
        result.withCookie(bruteForcePreventionModel)
      }

    } recover {
      case exception: Throwable =>
        Logger.error(
          s"Exception thrown by BruteForceService so for safety we won't let anyone through. " +
            s"Exception ${exception.getStackTraceString}"
        )
        Redirect(routes.MicroServiceError.present())
    } map { result =>
      result.withCookie(form)
    }


  def exit = Action {
    implicit request =>
      auditService.send(AuditMessage.from(
        pageMovement = AuditMessage.CaptureCertificateDetailsToExit,
        transactionId = request.cookies.getString(TransactionIdCacheKey).get,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel]))
      Redirect(routes.LeaveFeedback.present()).discardingCookies(removeCookiesOnExit)
  }

  private def formWithReplacedErrors(form: PlayForm[CaptureCertificateDetailsFormModel])(implicit request: Request[_]) =
    (form /: List(
      (ReferenceNumberId, "error.validReferenceNumber"),
      (PrVrmId, "error.validPrVrm"))) {
      (form, error) =>
        form.replaceError(error._1, FormError(
          key = error._1,
          message = error._2,
          args = Seq.empty
        ))
    }.distinctErrors

  /**
   * Call the eligibility service to determine if the VRM is valid for assignment
   */
  private def checkVrmEligibility(captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                                  vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                                  vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                                  transactionId: String)
                                 (implicit request: Request[_]): Future[Result] = {

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      auditService.send(AuditMessage.from(
        pageMovement = AuditMessage.CaptureCertificateDetailsToMicroServiceError,
        transactionId = transactionId,
        timestamp = dateService.dateTimeISOChronology
      ))
      Redirect(routes.MicroServiceError.present())
    }

    def eligibilitySuccess(certificateExpiryDate: DateTime) = {
      val redirectLocation = {
        auditService.send(AuditMessage.from(
          pageMovement = AuditMessage.CaptureCertificateDetailsToConfirm,
          transactionId = transactionId,
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel)))
        routes.Confirm.present()
      }

      // calculate number of years owed TODO tidy up after wireframe and wsdls
      var outstandingDates = new ListBuffer[String]
      var yearsOwedCount = 0
      var renewalExpiryDate = certificateExpiryDate
      var fmt = DateTimeFormat.forPattern("dd/MM/YYYY");
      while (renewalExpiryDate.isBeforeNow) {
        yearsOwedCount += 1
        outstandingDates += (fmt.print(renewalExpiryDate.minus(Period.years(1))) + " through to " + fmt.print(renewalExpiryDate) + " " + (config.renewalFee.toInt / 100.0))
        renewalExpiryDate = renewalExpiryDate.plus(Period.years(1))
      }

      bruteForceService.reset(captureCertificateDetailsFormModel.prVrm).onComplete {
        case scala.util.Success(httpCode) => Logger.debug(s"Brute force reset was called - it returned httpCode: $httpCode")
        case Failure(t) => Logger.error(s"Brute force reset failed: ${t.getStackTraceString}")
      }

      Redirect(redirectLocation)
        .withCookie(CaptureCertificateDetailsModel.from(Some(certificateExpiryDate), outstandingDates.toList, (yearsOwedCount * config.renewalFee.toInt)))
        .withCookie(captureCertificateDetailsFormModel)
    }

    def eligibilityFailure(responseCode: String) = {
      Logger.debug(s"VrmAssignEligibility encountered a problem with request" +
        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)}" +
        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}, redirect to VehicleLookupFailure")

      auditService.send(AuditMessage.from(
        pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
        transactionId = transactionId,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
        rejectionCode = Some(responseCode)))
      Redirect(routes.VehicleLookupFailure.present()).
        withCookie(key = VehicleAndKeeperLookupResponseCodeCacheKey, value = responseCode.split(" - ")(1))
    }

    val eligibilityRequest = VrmAssignEligibilityRequest(
      currentVehicleRegistrationMark = vehicleAndKeeperLookupFormModel.registrationNumber,
      certificateNumber = captureCertificateDetailsFormModel.referenceNumber,
      replacementVehicleRegistrationMark = captureCertificateDetailsFormModel.prVrm,
      v5DocumentReference = vehicleAndKeeperLookupFormModel.referenceNumber,
      transactionTimestamp = dateService.now.toDateTime
    )
    val trackingId = request.cookies.trackingId()

    eligibilityService.invoke(eligibilityRequest, trackingId).map {
      response =>
        response.responseCode match {
          case Some(responseCode) => eligibilityFailure(responseCode) // There is only a response code when there is a problem.
          case None =>
            // Happy path when there is no response code therefore no problem.
            response.certificateExpiryDate match {
              case Some(certificateExpiryDate) => eligibilitySuccess(certificateExpiryDate)
              case _ => microServiceErrorResult(message = "No lastDate found")
            }
        }
    }.recover {
      case NonFatal(e) =>
        microServiceErrorResult(s"Vrm Assign Eligibility web service call failed. Exception " + e.toString)
    }
  }
}