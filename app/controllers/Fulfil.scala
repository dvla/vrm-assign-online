package controllers

import audit._
import com.google.inject.Inject
import models._
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.mvc.{Result, _}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClientSideSessionFactory, CookieKeyValue}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import utils.helpers.Config
import views.vrm_assign.Confirm._
import views.vrm_assign.Fulfil._
import views.vrm_assign.VehicleLookup._
import webserviceclients.vrmassignfulfil.{VrmAssignFulfilResponse, VrmAssignFulfilRequest, VrmAssignFulfilService}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.util.control.NonFatal

final class Fulfil @Inject()(vrmAssignFulfilService: VrmAssignFulfilService,
                             dateService: DateService,
                             auditService: AuditService)
                            (implicit clientSideSessionFactory: ClientSideSessionFactory,
                             config: Config) extends Controller {

  def fulfil = Action.async { implicit request =>
    (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getString(TransactionIdCacheKey)) match {
      case (Some(vehiclesLookupForm), Some(transactionId)) =>
        fulfilVrm(vehiclesLookupForm, transactionId)
      case (_, Some(transactionId)) => {
        auditService.send(AuditMessage.from(
          pageMovement = AuditMessage.PaymentToMicroServiceError,
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

  private def fulfilVrm(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel, transactionId: String)
                       (implicit request: Request[_]): Future[Result] = {

    def fulfilSuccess() = {

      // create the transaction timestamp
      val transactionTimestamp = dateService.today.toDateTimeMillis.get
      val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp) + " " +
        ISODateTimeFormat.hourMinuteSecondMillis().print(transactionTimestamp)
      val transactionTimestampWithZone = s"$isoDateTimeString:${transactionTimestamp.getZone}"

      // TODO need to tidy this up!!
      val paymentModel = request.cookies.getModel[PaymentModel]

      if (paymentModel.isDefined) {

        paymentModel.get.paymentStatus = Some(Payment.SettledStatus)

        auditService.send(AuditMessage.from(
          pageMovement = AuditMessage.PaymentToSuccess,
          transactionId = request.cookies.getString(TransactionIdCacheKey).get,
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          keeperEmail = request.cookies.getString(KeeperEmailCacheKey),
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
          paymentModel = paymentModel))

        Redirect(routes.FulfilSuccess.present()).
          withCookie(paymentModel.get).
          withCookie(FulfilModel.from(transactionTimestampWithZone))
      } else {
        auditService.send(AuditMessage.from(
          pageMovement = AuditMessage.PaymentToSuccess,
          transactionId = request.cookies.getString(TransactionIdCacheKey).get,
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          keeperEmail = request.cookies.getString(KeeperEmailCacheKey),
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

      if (paymentModel.isDefined) {

        paymentModel.get.paymentStatus = Some(Payment.SettledStatus)

        auditService.send(AuditMessage.from(
          pageMovement = AuditMessage.PaymentToPaymentFailure,
          transactionId = request.cookies.getString(TransactionIdCacheKey).get,
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          keeperEmail = request.cookies.getString(KeeperEmailCacheKey),
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
                  paymentModel = paymentModel,
          rejectionCode = Some(responseCode)))

        Redirect(routes.FulfilFailure.present()).
                  withCookie(paymentModel.get).
          withCookie(key = FulfilResponseCodeCacheKey, value = responseCode.split(" - ")(1))
      } else {
        auditService.send(AuditMessage.from(
          pageMovement = AuditMessage.PaymentToPaymentFailure,
          transactionId = request.cookies.getString(TransactionIdCacheKey).get,
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          keeperEmail = request.cookies.getString(KeeperEmailCacheKey),
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
          rejectionCode = Some(responseCode)))

        Redirect(routes.FulfilFailure.present()).
          withCookie(key = FulfilResponseCodeCacheKey, value = responseCode.split(" - ")(1))
      }
    }

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      Redirect(routes.MicroServiceError.present())
    }

    val vrmAssignFulfilRequest = VrmAssignFulfilRequest(
      referenceNumber = vehicleAndKeeperLookupFormModel.registrationNumber, // TODO properly
      prVrm = vehicleAndKeeperLookupFormModel.registrationNumber,
      transactionTimestamp = dateService.today.toDateTimeMillis.get
    )
    val trackingId = request.cookies.trackingId()


    // TODO when wsdl becomes available
    vrmAssignFulfilService.invoke(vrmAssignFulfilRequest, trackingId).map {
      response =>
        response.responseCode match {
          case Some(responseCode) => fulfilFailure(responseCode) // There is only a response code when there is a problem.
          case None =>
            // Happy path when there is no response code therefore no problem.
//            response.certificateNumber match {
//              case Some(certificateNumber) =>
fulfilSuccess()
//              case _ =>
//                microServiceErrorResult(message = "Certificate number not found in response") // TODO tidy up when receive fulfil wsdl
//            }
        }
    }.recover {
      case NonFatal(e) =>
        microServiceErrorResult(s"VRM Retention Fulfil web service call failed. Exception " + e.toString)
    }
  }
}