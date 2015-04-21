package controllers

import com.google.inject.Inject
import models._
import org.joda.time.{DateTimeZone, DateTime}
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.mvc.Result
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebEndUserDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebHeaderDto
import utils.helpers.Config
import views.vrm_assign.Confirm._
import views.vrm_assign.Fulfil._
import views.vrm_assign.VehicleLookup._
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest
import webserviceclients.vrmassignfulfil.VrmAssignFulfilRequest
import webserviceclients.vrmassignfulfil.VrmAssignFulfilService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class Fulfil @Inject()(
                              vrmAssignFulfilService: VrmAssignFulfilService,
                              auditService2: audit2.AuditService
                              )
                            (implicit clientSideSessionFactory: ClientSideSessionFactory,
                             config: Config,
                             dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  def fulfil = Action.async { implicit request =>
    (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[CaptureCertificateDetailsFormModel],
      request.cookies.getString(GranteeConsentCacheKey)) match {
      case (Some(vehiclesLookupForm), Some(transactionId), Some(captureCertificateDetailsFormModel), Some(granteeConsent))
        if (granteeConsent == "true") =>
        fulfilVrm(vehiclesLookupForm, transactionId, captureCertificateDetailsFormModel)
      case (_, Some(transactionId), _, _) => {
        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.PaymentToMicroServiceError,
          transactionId = transactionId,
          timestamp = dateService.dateTimeISOChronology
        ))
        Future.successful {
          Redirect(routes.MicroServiceError.present())
        }
      }
      case _ =>
        Future.successful {
          Redirect(routes.Error.present("user went to fulfil mark without correct cookies"))
        }
    }
  }

  private def fulfilVrm(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel, transactionId: String,
                        captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel)
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
      replacementVehicleRegistrationMark = captureCertificateDetailsFormModel.prVrm,
      v5DocumentReference = vehicleAndKeeperLookupFormModel.referenceNumber,
      transactionTimestamp = dateService.now.toDateTime
    )

    vrmAssignFulfilService.invoke(vrmAssignFulfilRequest, trackingId).map {
      response =>
        response.responseCode match {
          case Some(responseCode) => fulfilFailure(responseCode) // There is only a response code when there is a problem.
          case None =>
            // Happy path when there is no response code therefore no problem.
            response.documentNumber match {
              case Some(documentNumber) =>
                fulfilSuccess
              case _ =>
                microServiceErrorResult(message = "Document number not found in response")
            }
        }
    }.recover {
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