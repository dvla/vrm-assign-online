package controllers

import com.google.inject.Inject
import models.{CaptureCertificateDetailsModel, CaptureCertificateDetailsViewModel, CaptureCertificateDetailsFormModel}
import models.{VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel}
import play.api.data.{Form, FormError}
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import utils.helpers.Config
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.CaptureCertificateDetails._
import views.vrm_assign.VehicleLookup._
import audit.{AuditMessage, AuditService}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import scala.concurrent.Future
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import scala.Some
import play.api.mvc.Result
import webserviceclients.vrmretentioneligibility.{VrmAssignEligibilityService, VrmAssignEligibilityRequest}
import org.joda.time.DateTime
import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext.Implicits.global


final class CaptureCertificateDetails @Inject()(eligibilityService: VrmAssignEligibilityService,
                                                auditService: AuditService, dateService: DateService)
                                               (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                config: Config) extends Controller {

  private[controllers] val form = Form(
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
          (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
            request.cookies.getModel[VehicleAndKeeperDetailsModel],
            request.cookies.getString(TransactionIdCacheKey)) match {
            case (Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetailsModel), Some(transactionId)) =>
              checkVrmEligibility(validForm, vehicleAndKeeperLookupFormModel, vehicleAndKeeperDetailsModel, transactionId)
            case _ =>
              Future.successful {
                Redirect(routes.MicroServiceError.present())
              } // TODO is this correct
          }
        }
      )
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

  private def formWithReplacedErrors(form: Form[CaptureCertificateDetailsFormModel])(implicit request: Request[_]) =
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

    def eligibilitySuccess(lastDate: DateTime) = {
      val redirectLocation = {
        auditService.send(AuditMessage.from(
          pageMovement = AuditMessage.CaptureCertificateDetailsToConfirm,
          transactionId = transactionId,
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel)))
        routes.Confirm.present()
      }
      Redirect(redirectLocation)
        .withCookie(CaptureCertificateDetailsModel.from(Some(lastDate)))
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
      referenceNumber = captureCertificateDetailsFormModel.referenceNumber,
      prVrm = captureCertificateDetailsFormModel.prVrm,
      transactionTimestamp = dateService.now.toDateTime
    )
    val trackingId = request.cookies.trackingId()

    eligibilityService.invoke(eligibilityRequest, trackingId).map {
      response =>
        response.responseCode match {
          case Some(responseCode) => eligibilityFailure(responseCode) // There is only a response code when there is a problem.
          case None =>
            // Happy path when there is no response code therefore no problem.
            response.lastDate match {
              case Some(lastDate) => eligibilitySuccess(lastDate)
              case _ => microServiceErrorResult(message = "No lastDate found")
            }
        }
    }.recover {
      case NonFatal(e) =>
        microServiceErrorResult(s"Vrm Assign Eligibility web service call failed. Exception " + e.toString)
    }
  }
}
