package controllers

import com.google.inject.Inject
import models.CacheKeyPrefix
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.Certificate
import models.FulfilModel
import models.SetupBusinessDetailsFormModel
import models.VehicleAndKeeperLookupFormModel
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.Period
import play.api.data.FormError
import play.api.data.{Form => PlayForm}
import play.api.libs.json.Writes
import play.api.mvc.{Action, Controller, Request, Result}
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.Failure
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichForm
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.model.{MicroserviceResponseModel, VehicleAndKeeperDetailsModel}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebEndUserDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebHeaderDto
import utils.helpers.Config
import views.vrm_assign.CaptureCertificateDetails.CertificateDateId
import views.vrm_assign.CaptureCertificateDetails.CertificateDocumentCountId
import views.vrm_assign.CaptureCertificateDetails.CertificateRegistrationMarkId
import views.vrm_assign.CaptureCertificateDetails.CertificateTimeId
import views.vrm_assign.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey
import views.vrm_assign.VehicleLookup.UserType_Business
import views.vrm_assign.VehicleLookup.UserType_Keeper
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityRequest
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityResponse
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityService
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityResponseDto

final class CaptureCertificateDetails @Inject()(val bruteForceService: BruteForcePreventionService,
                                                 eligibilityService: VrmAssignEligibilityService,
                                                 auditService2: audit2.AuditService)
                                               (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                config: Config,
                                                dateService: DateService)
                                                extends Controller with DVLALogger {

  type Form = CaptureCertificateDetailsFormModel

  private[controllers] val form = PlayForm(
    CaptureCertificateDetailsFormModel.Form.Mapping
  )

  def present = Action {
    implicit request =>
      (request.cookies.getModel[VehicleAndKeeperDetailsModel],
        request.cookies.getModel[VehicleAndKeeperLookupFormModel],
        request.cookies.getModel[SetupBusinessDetailsFormModel],
        request.cookies.getString(StoreBusinessDetailsCacheKey),
        request.cookies.getModel[FulfilModel]) match {
        case (Some(vehicleAndKeeperDetails), Some(vehicleAndKeeperLookupForm), Some(setupBusinessDetailsFormModel),
        Some(storeBusinessDetails), None) if vehicleAndKeeperLookupForm.userType == UserType_Business =>
          // Happy path for a business user that has all the cookies
          logMessage(request.cookies.trackingId(), Info, s"Presenting capture certificate details view")
          Ok(views.html.vrm_assign.capture_certificate_details(form.fill(), vehicleAndKeeperDetails))
        case (Some(vehicleAndKeeperDetails), Some(vehicleAndKeeperLookupForm), _, _, None)
          if vehicleAndKeeperLookupForm.userType == UserType_Keeper =>

          // They are not a business, so we only need the VehicleAndKeeperDetailsModel
          logMessage(request.cookies.trackingId(), Info, s"Presenting capture certificate details view")
          Ok(views.html.vrm_assign.capture_certificate_details(form.fill(), vehicleAndKeeperDetails))
        case _ =>
          logMessage(request.cookies.trackingId(), Warn,
            "CaptureCertificateDetails present is missing cookies, now redirecting")
          Redirect(routes.ConfirmBusiness.present())
      }
  }

  def submit = Action.async {
    implicit request =>
      form.bindFromRequest.fold(
        invalidForm => {
          request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
            case Some(vehicleAndKeeperDetails) =>
              Future.successful {
                BadRequest(views.html.vrm_assign.capture_certificate_details(formWithReplacedErrors(invalidForm),
                   vehicleAndKeeperDetails))
              }
            case _ =>
              Future.successful {
                val msg = "user went to CaptureCertificateDetails submit without the " +
                  "VehicleAndKeeperDetailsModel cookie"
                Redirect(routes.Error.present(msg))
              }
          }
        },
        validForm => {
          val vehicleAndKeeperLookupFormOpt= request.cookies.getModel[VehicleAndKeeperLookupFormModel]
          vehicleAndKeeperLookupFormOpt match {
            case Some(vehicleAndKeeperLookupForm) =>
              bruteForceAndLookup(vehicleAndKeeperLookupForm.replacementVRN, validForm)
            case _ =>  Future.successful( Redirect(routes.MicroServiceError.present()) )
          }
        }
      )
  }

  def bruteForceAndLookup(prVrm: String, form: Form)
                         (implicit request: Request[_],
                          toJson: Writes[Form],
                          cacheKey: CacheKey[Form]
                           ): Future[Result] =
    bruteForceService.isVrmLookupPermitted(prVrm, request.cookies.trackingId()).flatMap { bruteForcePreventionModel =>
      val resultFuture = if (bruteForcePreventionModel.permitted) {
        val vehicleAndKeeperLookupFormModel = request.cookies.getModel[VehicleAndKeeperLookupFormModel].get
        val vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel].get
        val transactionId = request.cookies.getString(TransactionIdCacheKey)
          .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value)
        checkVrmEligibility(form, vehicleAndKeeperLookupFormModel, vehicleAndKeeperDetailsModel, transactionId)
      }
      else Future.successful {
        val anonRegistrationNumber = LogFormats.anonymize(prVrm)
        logMessage(request.cookies.trackingId(), Warn, s"BruteForceService locked out vrm: $anonRegistrationNumber")
        Redirect(routes.VrmLocked.present())
      }
      resultFuture.map { result =>
        result.withCookie(bruteForcePreventionModel)
      }
    } recover {
      case exception: Throwable =>
        logMessage(request.cookies.trackingId(), Error,
          s"Exception thrown by BruteForceService so for safety we won't let anyone through. ${exception.getMessage}"
        )
        Redirect(routes.MicroServiceError.present())
    } map { result =>
      result.withCookie(form)
    }

  /**
   * Call the eligibility service to determine if the VRM is valid for assignment
   */
  private def checkVrmEligibility(captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                                  vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                                  vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                                  transactionId: String)
                                 (implicit request: Request[_]): Future[Result] = {

    val trackingId = request.cookies.trackingId()

    def microServiceErrorResult(message: String) = {
      logMessage(request.cookies.trackingId(), Error, message)
      auditService2.send(AuditRequest.from(
        pageMovement = AuditRequest.CaptureCertificateDetailsToMicroServiceError,
        transactionId = transactionId,
        timestamp = dateService.dateTimeISOChronology
      ), trackingId)
      Redirect(routes.MicroServiceError.present())
    }

    def eligibilitySuccess(certificate: Certificate) = {
      logMessage(trackingId, Debug, "Eligibility check was successful")

      val captureCertificateDetailsModel = CaptureCertificateDetailsModel.from(
        vehicleAndKeeperLookupFormModel.replacementVRN,
        certificate
       )

      val redirectLocation = {
        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.CaptureCertificateDetailsToConfirm,
          transactionId = transactionId,
          timestamp = dateService.dateTimeISOChronology,
          documentReferenceNumber = Some(vehicleAndKeeperLookupFormModel.referenceNumber),
          vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
          captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
          captureCertificateDetailsModel = Some(captureCertificateDetailsModel)), trackingId
        )
        routes.Confirm.present()
      }

      bruteForceService.reset(vehicleAndKeeperLookupFormModel.replacementVRN, trackingId).onComplete {
        case scala.util.Success(httpCode) => logMessage(trackingId, Debug,
          s"Brute force reset was called - it returned httpCode: $httpCode")
        case Failure(ex) => logMessage(trackingId, Error, s"Brute force reset failed: ${ex.getMessage}")
      }

      Redirect(redirectLocation)
        .withCookie(captureCertificateDetailsModel)
        .withCookie(captureCertificateDetailsFormModel)
    }

    def eligibilityFailure(failure: VrmAssignEligibilityResponseDto, certificate: Certificate) = {
      val response = failure.response.get

      val msg = "Eligibility check failed for request " +
        s"referenceNumber = ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)}, " +
        s"registrationNumber = ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}" +
        ", redirecting to VehicleLookupFailure"
      logMessage(trackingId, Debug, msg)

      val captureCertificateDetailsModel = CaptureCertificateDetailsModel.from(
        vehicleAndKeeperLookupFormModel.replacementVRN,
        certificate
      )

      auditService2.send(AuditRequest.from(
        pageMovement = AuditRequest.CaptureCertificateDetailsToCaptureCertificateDetailsFailure,
        transactionId = transactionId,
        timestamp = dateService.dateTimeISOChronology,
        documentReferenceNumber = Some(vehicleAndKeeperLookupFormModel.referenceNumber),
        vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
        captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
        captureCertificateDetailsModel = Some(captureCertificateDetailsModel),
        rejectionCode = Some(s"${response.code} - ${response.message}")),
        trackingId = trackingId
      )

      Redirect(routes.VehicleLookupFailure.present())
        .withCookie(MicroserviceResponseModel.content(response))
        .withCookie(captureCertificateDetailsModel)
    }

    val eligibilityRequest = VrmAssignEligibilityRequest(
      buildWebHeader(trackingId, request.cookies.getString(models.IdentifierCacheKey)),
      currentVehicleRegistrationMark = vehicleAndKeeperLookupFormModel.registrationNumber,
      certificateDate = captureCertificateDetailsFormModel.certificateDate,
      certificateTime = captureCertificateDetailsFormModel.certificateTime,
      certificateDocumentCount = captureCertificateDetailsFormModel.certificateDocumentCount,
      certificateRegistrationMark = captureCertificateDetailsFormModel.certificateRegistrationMark,
      replacementVehicleRegistrationMark = vehicleAndKeeperLookupFormModel.replacementVRN,
      v5DocumentReference = vehicleAndKeeperLookupFormModel.referenceNumber,
      transactionTimestamp = dateService.now.toDateTime
    )

    eligibilityService.invoke(eligibilityRequest, trackingId).map {
      response => {
        response match {
          // Only need to calculate certificate expiry for direct to paper failures
          case (FORBIDDEN, failure) if failure.response.get.message == "vrm_assign_eligibility_direct_to_paper" =>
            eligibilityFailure(failure, validateCertificate(failure.vrmAssignEligibilityResponse.certificateExpiryDate))
          case (FORBIDDEN, failure) =>
            eligibilityFailure(failure, Certificate.Unknown)
          case (OK, success) => {
            val expiryDate = success.vrmAssignEligibilityResponse.certificateExpiryDate
            validateCertificate(expiryDate) match {
              case certificate: Certificate.Expired => eligibilityFailure(VrmAssignEligibilityResponseDto(
                Some(MicroserviceResponse("T1045", "vrm_assign_eligibility_cert_expired")),
                VrmAssignEligibilityResponse(expiryDate)
              ), certificate)
              case certificate @ _ => eligibilitySuccess(certificate)
            }
          }
        }
      }
    }.recover {
      case NonFatal(e) =>
        microServiceErrorResult(s"Vrm Assign Eligibility web service call failed. Exception: " + e.toString)
    }
  }

  private def buildWebHeader(trackingId: TrackingId,
                             identifier: Option[String]): VssWebHeaderDto =
    VssWebHeaderDto(transactionId = trackingId.value,
      originDateTime = dateService.now.toDateTime,
      applicationCode = config.applicationCode,
      serviceTypeCode = config.vssServiceTypeCode,
      buildEndUser(identifier))

  private def buildEndUser(identifier: Option[String]): VssWebEndUserDto =
    VssWebEndUserDto(endUserId = identifier.getOrElse(config.orgBusinessUnit), orgBusUnit = config.orgBusinessUnit)

  private def formWithReplacedErrors(form: PlayForm[CaptureCertificateDetailsFormModel])
                                    (implicit request: Request[_]) = {
    val certificateTimeWithSummary = FormError(
      key = CertificateTimeId,
      messages = Seq("error.summary-validCertificateTime", "error.validCertificateTime"),
      args = Seq.empty
    )

    val replacedErrors = (form /: List(
      (CertificateDocumentCountId, "error.validCertificateDocumentCount"),
      (CertificateDateId, "error.validCertificateDate"),
      (CertificateRegistrationMarkId, "error.restricted.validVrnOnly"))) {
      (form, error) =>
        form.replaceError(error._1, FormError(
          key = error._1,
          message = error._2,
          args = Seq.empty
        ))
    }
      .replaceError(
        CertificateTimeId,
        certificateTimeWithSummary
      ).distinctErrors

    replacedErrors
  }

  def back = Action { implicit request =>
    // If the user is a business actor, then navigate to the previous page in the business journey,
    // Else the user is a keeper actor, then navigate to the previous page in the keeper journey
    val businessPath = for {
      vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      if vehicleAndKeeperLookupForm.userType == UserType_Business
    } yield {
        Redirect(routes.ConfirmBusiness.present())
      }
    val keeperPath = Redirect(routes.VehicleLookup.present())
    businessPath.getOrElse(keeperPath)
  }

  def exit = Action { implicit request =>
    val trackingId = request.cookies.trackingId()
    auditService2.send(AuditRequest.from(
      pageMovement = AuditRequest.CaptureCertificateDetailsToExit,
      transactionId = request.cookies.getString(TransactionIdCacheKey).
        getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
      timestamp = dateService.dateTimeISOChronology,
      documentReferenceNumber = request.cookies.getModel[VehicleAndKeeperLookupFormModel].map(_.referenceNumber),
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel]), trackingId
    )
    Redirect(routes.LeaveFeedback.present()).discardingCookies(removeCookiesOnExit)
  }

  def validateCertificate(certExpiryDate: Option[DateTime]) = {
    val today = dateService.now.toDateTime.toLocalDate

    def canMakeOneOffPayment(expiryDate: DateTime) =
      !expiryDate.toLocalDate.isBefore(today.minusYears(config.renewalFeeExpiryInYears))

    certExpiryDate match {
      case Some(expiryDate) if (!expiryDate.toLocalDate.isBefore(today)) => Certificate.Valid(expiryDate)
      case Some(expiryDate) if (canMakeOneOffPayment(expiryDate)) =>
        Certificate.ExpiredWithFee(expiryDate, config.renewalFeeInPence.toInt, f"${config.renewalFeeInPence.toDouble / 100.0}%.2f")
      case Some(expiryDate) => Certificate.Expired(expiryDate)
      case None => Certificate.Unknown
    }
  }
}
