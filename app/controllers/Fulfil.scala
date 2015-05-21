package controllers

import com.google.inject.Inject
import controllers.Payment.AuthorisedStatus
import java.util.concurrent.TimeoutException
import models.BusinessDetailsModel
import models.CacheKeyPrefix
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.ConfirmFormModel
import models.FulfilModel
import models.PaymentModel
import models.VehicleAndKeeperLookupFormModel
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import play.api.Logger
import play.api.mvc.{Action, Controller, Request, Result}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebEndUserDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebHeaderDto
import utils.helpers.Config
import views.vrm_assign.Confirm.GranteeConsentCacheKey
import views.vrm_assign.Fulfil.FulfilResponseCodeCacheKey
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey
import views.vrm_assign.Payment.PaymentTransNoCacheKey
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest
import webserviceclients.vrmassignfulfil.VrmAssignFulfilRequest
import webserviceclients.vrmassignfulfil.VrmAssignFulfilService

final class Fulfil @Inject()(vrmAssignFulfilService: VrmAssignFulfilService,
                              auditService2: audit2.AuditService
                              )
                            (implicit clientSideSessionFactory: ClientSideSessionFactory,
                             config: Config,
                             dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  def fulfil = Action.async { implicit request =>
    (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[CaptureCertificateDetailsFormModel],
      request.cookies.getString(GranteeConsentCacheKey),
      request.cookies.getModel[CaptureCertificateDetailsModel],
      request.cookies.getString(PaymentTransNoCacheKey),
      request.cookies.getModel[PaymentModel]
      ) match {
      case (Some(vehiclesLookupForm), Some(transactionId), Some(captureCertificateDetailsFormModel), Some(granteeConsent),
      Some(captureCertificateDetails), Some(paymentTransNo), Some(payment))
        if granteeConsent == "true" && (captureCertificateDetails.outstandingFees > 0 && payment.paymentStatus == Some(AuthorisedStatus)) =>
        fulfilVrm(vehiclesLookupForm, transactionId, captureCertificateDetailsFormModel,
          Some(paymentTransNo), Some(payment.trxRef.get), Some(payment.isPrimaryUrl))
      case (Some(vehiclesLookupForm), Some(transactionId), Some(captureCertificateDetailsFormModel), Some(granteeConsent),
      Some(captureCertificateDetails), _, _)
        if granteeConsent == "true" && (captureCertificateDetails.outstandingFees == 0) =>
        fulfilVrm(vehiclesLookupForm, transactionId, captureCertificateDetailsFormModel, None, None, None)
      case _ =>
        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.PaymentToMicroServiceError,
          transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
          timestamp = dateService.dateTimeISOChronology
        ))
        Future.successful {
          Redirect(routes.Error.present("user went to fulfil mark without correct cookies"))
        }
    }
  }

  private def fulfilVrm(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel, transactionId: String,
                        captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                        paymentTransNo: Option[String], paymentTrxRef: Option[String],
                        isPaymentPrimaryUrl: Option[Boolean])
                       (implicit request: Request[_]): Future[Result] = {

    def fulfilSuccess() = {

      // create the transaction timestamp
      val transactionTimestamp =
        DayMonthYear.from(new DateTime(dateService.now, DateTimeZone.forID("Europe/London"))).toDateTimeMillis.get
      val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp) + " " +
        ISODateTimeFormat.hourMinuteSecond().print(transactionTimestamp)
      val transactionTimestampWithZone = s"$isoDateTimeString"

      // TODO need to tidy this up!!
      val paymentModel = request.cookies.getModel[PaymentModel]

      // if no payment model then no outstanding fees
      if (paymentModel.isDefined) {

        paymentModel.get.paymentStatus = Some(Payment.SettledStatus)

        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.PaymentToSuccess,
          transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
          captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
          captureCertificateDetailsModel = request.cookies.getModel[CaptureCertificateDetailsModel],
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
          paymentModel = paymentModel))

        Redirect(routes.FulfilSuccess.present()).
          withCookie(paymentModel.get).
          withCookie(FulfilModel.from(transactionTimestampWithZone))
      } else {
        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.ConfirmToSuccess,
          transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
          captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
          captureCertificateDetailsModel = request.cookies.getModel[CaptureCertificateDetailsModel],
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

        Redirect(routes.FulfilSuccess.present()).
          withCookie(FulfilModel.from(transactionTimestampWithZone))
      }
    }

    def fulfilFailure(responseCode: String) = {
      Logger.debug(s"VRMRetentionFulfil encountered a problem with request" +
        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)}" +
        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}," +
        s" redirect to VehicleLookupFailure")

      // TODO need to tidy this up!!
      val paymentModel = request.cookies.getModel[PaymentModel]
      val captureCertificateDetailsFormModel = request.cookies.getModel[CaptureCertificateDetailsFormModel].get
      val captureCertificateDetails = request.cookies.getModel[CaptureCertificateDetailsModel].get

      if (paymentModel.isDefined) {

        paymentModel.get.paymentStatus = Some(Payment.SettledStatus)

        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.PaymentToPaymentFailure,
          transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
          paymentModel = paymentModel,
          captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
          captureCertificateDetailsModel = Some(captureCertificateDetails),
          rejectionCode = Some(responseCode)))

        Redirect(routes.FulfilFailure.present()).
          withCookie(paymentModel.get).
          withCookie(key = FulfilResponseCodeCacheKey, value = responseCode.split(" - ")(1))
      } else {
        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.ConfirmToFulfilFailure,
          transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
          captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
          captureCertificateDetailsModel = Some(captureCertificateDetails),
          rejectionCode = Some(responseCode)))

        Redirect(routes.FulfilFailure.present()).
          withCookie(key = FulfilResponseCodeCacheKey, value = responseCode.split(" - ")(1))
      }
    }

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      Redirect(routes.MicroServiceError.present())
    }

    val trackingId = request.cookies.trackingId()

    val vrmAssignFulfilRequest = VrmAssignFulfilRequest(
      buildWebHeader(trackingId),
      currentVehicleRegistrationMark = vehicleAndKeeperLookupFormModel.registrationNumber,
      certificateDate = captureCertificateDetailsFormModel.certificateDate,
      certificateTime = captureCertificateDetailsFormModel.certificateTime,
      certificateDocumentCount = captureCertificateDetailsFormModel.certificateDocumentCount,
      certificateRegistrationMark = captureCertificateDetailsFormModel.certificateRegistrationMark,
      replacementVehicleRegistrationMark = vehicleAndKeeperLookupFormModel.replacementVRN,
      v5DocumentReference = vehicleAndKeeperLookupFormModel.referenceNumber,
      transactionTimestamp = dateService.now.toDateTime,
      paymentTransNo = paymentTransNo,
      paymentTrxRef = paymentTrxRef,
      isPaymentPrimaryUrl = isPaymentPrimaryUrl)

    vrmAssignFulfilService.invoke(vrmAssignFulfilRequest, trackingId).map {
      response =>
        response.responseCode match {
          case Some(responseCode) => fulfilFailure(responseCode) // There is only a response code when there is a problem.
          case None =>
            // Happy path when there is no response code therefore no problem.
            response.documentNumber match {
              case Some(documentNumber) =>
                fulfilSuccess()
              case _ =>
                microServiceErrorResult(message = "Document number not found in response")
            }
        }
    }.recover {
      case _: TimeoutException =>  Redirect(routes.TimeoutController.present())
      case NonFatal(e) =>
        microServiceErrorResult(s"VRM Assign Fulfil web service call failed. Exception " + e.toString)
    }
  }

  private def buildWebHeader(trackingId: String): VssWebHeaderDto = {
    VssWebHeaderDto(transactionId = trackingId,
      originDateTime = new DateTime,
      applicationCode = config.applicationCode,
      serviceTypeCode = config.vssServiceTypeCode,
      buildEndUser())
  }

  private def buildEndUser(): VssWebEndUserDto = {
    VssWebEndUserDto(endUserId = config.orgBusinessUnit, orgBusUnit = config.orgBusinessUnit)
  }
}