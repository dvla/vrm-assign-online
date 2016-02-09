package controllers

import com.google.inject.Inject
import controllers.Payment.AuthorisedStatus
import email.{FailureEmailMessageBuilder, AssignEmailService, ReceiptEmailMessageBuilder}
import java.util.concurrent.TimeoutException
import models.BusinessDetailsModel
import models.CacheKeyPrefix
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.ConfirmFormModel
import models.FulfilModel
import models.PaymentModel
import models.VehicleAndKeeperLookupFormModel
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import play.api.i18n.Messages
import play.api.mvc.{Action, Controller, Request, Result}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.services.SEND.Contents
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebEndUserDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebHeaderDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailServiceSendRequest
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.From
import utils.helpers.Config
import views.vrm_assign.Confirm.GranteeConsentCacheKey
import views.vrm_assign.Fulfil.FulfilResponseCodeCacheKey
import views.vrm_assign.Payment.PaymentTransNoCacheKey
import views.vrm_assign.VehicleLookup.{TransactionIdCacheKey, UserType_Business}
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest
import webserviceclients.paymentsolve.PaymentSolveUpdateRequest
import webserviceclients.vrmassignfulfil.VrmAssignFulfilRequest
import webserviceclients.vrmassignfulfil.VrmAssignFulfilService

final class Fulfil @Inject()(vrmAssignFulfilService: VrmAssignFulfilService,
                             auditService2: audit2.AuditService,
                             assignEmailService: AssignEmailService)
                            (implicit clientSideSessionFactory: ClientSideSessionFactory,
                             config: Config,
                             dateService: DateService) extends Controller with DVLALogger {

  private val SETTLE_AUTH_CODE = "Settle"

  def fulfil = Action.async { implicit request =>
    (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[CaptureCertificateDetailsFormModel],
      request.cookies.getString(GranteeConsentCacheKey),
      request.cookies.getModel[CaptureCertificateDetailsModel],
      request.cookies.getString(PaymentTransNoCacheKey),
      request.cookies.getModel[PaymentModel]) match {
      case (Some(vehiclesLookupForm),
      Some(transactionId),
      Some(captureCertificateDetailsFormModel),
      Some(granteeConsent),
      Some(captureCertificateDetails),
      Some(paymentTransNo),
      Some(payment)) if granteeConsent == "true" &&
        captureCertificateDetails.outstandingFees > 0 &&
        payment.paymentStatus == Some(AuthorisedStatus) =>
        fulfilVrm(
          request,
          vehiclesLookupForm,
          transactionId,
          captureCertificateDetailsFormModel,
          captureCertificateDetails,
          Some(paymentTransNo),
          Some(payment)
        )
      case (Some(vehiclesLookupForm),
      Some(transactionId),
      Some(captureCertificateDetailsFormModel),
      Some(granteeConsent),
      Some(captureCertificateDetails),
      _,
      _) if granteeConsent == "true" &&
        captureCertificateDetails.outstandingFees == 0 =>
        fulfilVrm(
          request,
          vehiclesLookupForm,
          transactionId,
          captureCertificateDetailsFormModel,
          captureCertificateDetails,
          None,
          None
        )
      case _ =>
        val trackingId = request.cookies.trackingId()
        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.PaymentToMicroServiceError,
          transactionId = request.cookies
            .getString(TransactionIdCacheKey)
            .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
          timestamp = dateService.dateTimeISOChronology
        ), trackingId)
        Future.successful {
          Redirect(routes.Error.present("user went to fulfil mark without correct cookies"))
        }
    }
  }

  private def fulfilVrm(implicit request: Request[_],
                        vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                        transactionId: String,
                        captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                        captureCertificateDetails: CaptureCertificateDetailsModel,
                        paymentTransNo: Option[String],
                        paymentModel: Option[PaymentModel]): Future[Result] = {

    // create the transaction timestamp
    val transactionTimestamp =
      DayMonthYear.from(new DateTime(dateService.now, DateTimeZone.forID("Europe/London"))).toDateTimeMillis.get
    val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp) + " " +
      ISODateTimeFormat.hourMinuteSecond().print(transactionTimestamp)
    val transactionTimestampWithZone = s"$isoDateTimeString"

    def fulfilSuccess() = {
      val trackingId = request.cookies.trackingId()
      logMessage(trackingId, Info, "Vrm assign fulfil micro-service returned success")
      // if no payment model then no outstanding fees
      paymentModel match {
        case Some(payment) =>
          paymentModel.get.paymentStatus = Some(Payment.SettledStatus)

          auditService2.send(AuditRequest.from(
            pageMovement = AuditRequest.PaymentToSuccess,
            transactionId = request.cookies
              .getString(TransactionIdCacheKey)
              .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
            timestamp = dateService.dateTimeISOChronology,
            vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
            keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
            captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
            captureCertificateDetailsModel = request.cookies.getModel[CaptureCertificateDetailsModel],
            businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
            paymentModel = paymentModel), trackingId
          )

          Redirect(routes.FulfilSuccess.present())
            .withCookie(paymentModel.get)
            .withCookie(FulfilModel.from(transactionTimestampWithZone))
        case _ =>
          auditService2.send(AuditRequest.from(
            pageMovement = AuditRequest.ConfirmToSuccess,
            transactionId = request.cookies.getString(TransactionIdCacheKey).
              getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
            timestamp = dateService.dateTimeISOChronology,
            vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
            keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
            captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
            captureCertificateDetailsModel = request.cookies.getModel[CaptureCertificateDetailsModel],
            businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]), trackingId
          )

          Redirect(routes.FulfilSuccess.present()).withCookie(FulfilModel.from(transactionTimestampWithZone))
      }
    }

    def fulfilFailure(response: MicroserviceResponse) = {
      logMessage(request.cookies.trackingId, Debug, "Vrm assign fulfil micro-service failed for request with " +
        s"referenceNumber = ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)}, "  +
        s"registrationNumber = ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}, " +
        "redirecting to FulfilFailure")

      val captureCertificateDetailsFormModel = request.cookies.getModel[CaptureCertificateDetailsFormModel].get
      val captureCertificateDetails = request.cookies.getModel[CaptureCertificateDetailsModel].get
      val trackingId = request.cookies.trackingId()
      val responseCode = s"${response.code} - ${response.message}"

      if (paymentModel.isDefined) {
        paymentModel.get.paymentStatus = Some(Payment.SettledStatus)

        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.PaymentToPaymentFailure,
          transactionId = request.cookies
            .getString(TransactionIdCacheKey)
            .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
          paymentModel = paymentModel,
          captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
          captureCertificateDetailsModel = Some(captureCertificateDetails),
          rejectionCode = Some(responseCode)), trackingId
        )

        Redirect(routes.FulfilFailure.present())
          .withCookie(paymentModel.get)
          .withCookie(key = FulfilResponseCodeCacheKey, value = response.message)
      } else {
        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.ConfirmToFulfilFailure,
          transactionId = request.cookies
            .getString(TransactionIdCacheKey)
            .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
          captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
          captureCertificateDetailsModel = Some(captureCertificateDetails),
          rejectionCode = Some(responseCode)), trackingId
        )

        Redirect(routes.FulfilFailure.present())
          .withCookie(key = FulfilResponseCodeCacheKey, value = response.message)
      }
    }

    def microServiceErrorResult(message: String) = {
      logMessage(request.cookies.trackingId, Error, message)
      Redirect(routes.MicroServiceError.present())
    }

    def fulfillConfirmEmail(implicit request: Request[_]): Seq[EmailServiceSendRequest] = {
      val trackingId = request.cookies.trackingId()
      request.cookies.getModel[VehicleAndKeeperDetailsModel] match {

        case Some(vehicleAndKeeperDetails) =>
          val businessDetailsOpt = request.cookies.getModel[BusinessDetailsModel].
            filter(_ => vehicleAndKeeperLookupFormModel.userType == UserType_Business)
          val keeperEmailOpt = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail)
          val confirmFormModel = request.cookies.getModel[ConfirmFormModel]
          val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]

          businessDetailsOpt.fold {logMessage(request.cookies.trackingId, Debug,
            "No business details cookie found or user type not business so will " +
            "not create a fulfil confirm email for the business")
          }
          {keeperEmail => logMessage(request.cookies.trackingId, Debug,
            "Business details cookie found and user type is business so will " +
            "create a fulfil confirm email for the business")}
          keeperEmailOpt.fold {logMessage(request.cookies.trackingId, Debug,
            "No keeper email supplied so will not create a fulfil confirm email for the keeper")
          }
          {keeperEmail => logMessage(request.cookies.trackingId, Debug,
            "Keeper email supplied so will create a fulfil confirm email for the keeper")}

          Seq(businessDetailsOpt.flatMap { businessDetails =>
            assignEmailService.emailRequest(
              businessDetails.email,
              vehicleAndKeeperDetails,
              captureCertificateDetailsFormModel,
              captureCertificateDetails,
              vehicleAndKeeperLookupFormModel,
              transactionTimestampWithZone,
              transactionId,
              confirmFormModel,
              businessDetailsModel,
              isKeeper = false, // US1589: Do not send keeper a pdf
              trackingId = trackingId
            )
          },
            keeperEmailOpt.flatMap { keeperEmail =>
              assignEmailService.emailRequest(
                keeperEmail,
                vehicleAndKeeperDetails,
                captureCertificateDetailsFormModel,
                captureCertificateDetails,
                vehicleAndKeeperLookupFormModel,
                transactionTimestampWithZone,
                transactionId,
                confirmFormModel,
                businessDetailsModel,
                isKeeper = true,
                trackingId = trackingId
              )
            }).flatten
        case _ => Seq.empty
      }
    }

    val trackingId = request.cookies.trackingId()
    val vrmAssignFulfilRequest = VrmAssignFulfilRequest(
      buildWebHeader(trackingId, request.cookies.getString(models.IdentifierCacheKey)),
      currentVehicleRegistrationMark = vehicleAndKeeperLookupFormModel.registrationNumber,
      certificateDate = captureCertificateDetailsFormModel.certificateDate,
      certificateTime = captureCertificateDetailsFormModel.certificateTime,
      certificateDocumentCount = captureCertificateDetailsFormModel.certificateDocumentCount,
      certificateRegistrationMark = captureCertificateDetailsFormModel.certificateRegistrationMark,
      replacementVehicleRegistrationMark = vehicleAndKeeperLookupFormModel.replacementVRN,
      v5DocumentReference = vehicleAndKeeperLookupFormModel.referenceNumber,
      transactionTimestamp = dateService.now.toDateTime,
      paymentSolveUpdateRequest = paymentModel match {
        case Some(payment) =>
          Some(buildPaymentSolveUpdateRequest(captureCertificateDetails, paymentTransNo.get, payment.trxRef.get,
            SETTLE_AUTH_CODE, payment.isPrimaryUrl, vehicleAndKeeperLookupFormModel, transactionId, trackingId))
        case _ => None
      },
      successEmailRequests = fulfillConfirmEmail,
      failureEmailRequests = buildPaymentFailureEmailRequests(vehicleAndKeeperLookupFormModel, trackingId)
    )

    vrmAssignFulfilService.invoke(vrmAssignFulfilRequest, trackingId).map {
      case (FORBIDDEN, failure) => fulfilFailure(failure.response.get)
      case (OK, success) =>
        success.vrmAssignFulfilResponse.documentNumber match {
          case Some(documentNumber) =>
            fulfilSuccess()
          case _ =>
            microServiceErrorResult(message = "Document number not found in response")
        }
    }.recover {
      case _: TimeoutException =>  Redirect(routes.TimeoutController.present())
      case NonFatal(e) =>
        microServiceErrorResult(s"VRM Assign Fulfil web service call failed. Exception " + e.toString)
    }
  }

  private def buildWebHeader(trackingId: TrackingId,
                             identifier: Option[String]): VssWebHeaderDto = {
    VssWebHeaderDto(transactionId = trackingId.value,
      originDateTime = new DateTime,
      applicationCode = config.applicationCode,
      serviceTypeCode = config.vssServiceTypeCode,
      buildEndUser(identifier))
  }

  private def buildEndUser(identifier: Option[String]): VssWebEndUserDto = {
    VssWebEndUserDto(endUserId = identifier.getOrElse(config.orgBusinessUnit), orgBusUnit = config.orgBusinessUnit)
  }

  private def buildPaymentSolveUpdateRequest(captureCertificateDetails: CaptureCertificateDetailsModel,
                                             paymentTransNo: String, paymentTrxRef: String,
                                             authType: String, isPaymentPrimaryUrl: Boolean,
                                             vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                                             transactionId: String,
                                             trackingId: TrackingId)
                                            (implicit request: Request[_]):
  PaymentSolveUpdateRequest = {
    PaymentSolveUpdateRequest(
      paymentTransNo,
      paymentTrxRef,
      authType,
      isPaymentPrimaryUrl,
      buildPaymentSuccessEmailRequests(
        captureCertificateDetails,
        vehicleAndKeeperLookupFormModel,
        transactionId,
        trackingId
      )
    )
  }

  private def buildPaymentFailureEmailRequests(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                                               trackingId: TrackingId)
                                              (implicit request: Request[_]): List[EmailServiceSendRequest] = {

    val template = FailureEmailMessageBuilder.buildWith

    val title = Messages("email.failure.title") + " "+ vehicleAndKeeperLookupFormModel.registrationNumber

    val from = From(config.emailConfiguration.from.email, config.emailConfiguration.from.name)

    val confirmFormModel = request.cookies.getModel[ConfirmFormModel]

    val isKeeperUserType = vehicleAndKeeperLookupFormModel.isKeeperUserType
    logMessage(trackingId, Debug, s"isKeeperUserType = $isKeeperUserType")

    // send keeper email if present
    val keeperEmail: Option[EmailServiceSendRequest] = if (isKeeperUserType) {
      for {
        model <- confirmFormModel
        email <- model.keeperEmail
      } yield {
        logMessage(trackingId, Debug, "We are going to create a payment failure email for the keeper user type")
        buildEmailServiceSendRequest(template, from, title, email)
      }
    } else {
      logMessage(trackingId, Debug, "We are not going to create a payment failure email for the keeper user type " +
        "because we are not dealing with the keeper user type")
      None
    }

    if (keeperEmail.isEmpty &&
      isKeeperUserType && confirmFormModel.nonEmpty &&
      confirmFormModel.get.keeperEmail.isEmpty
    ) {
      logMessage(trackingId, Debug, "We are not going to create a payment failure email for the keeper user type " +
        "because no email was supplied")
    }

    val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]
    val isBusinessUserType = vehicleAndKeeperLookupFormModel.isBusinessUserType
    logMessage(trackingId, Debug, s"isBusinessUserType = $isBusinessUserType")

    // send business email if present
    val businessEmail: Option[EmailServiceSendRequest] = if (isBusinessUserType) {
      for {
        model <- businessDetailsModel
      } yield {
        logMessage(trackingId, Debug, "We are going to create a payment failure email for the business user type")
        buildEmailServiceSendRequest(template, from, title, model.email)
      }
    } else {
      logMessage(trackingId, Debug, "We are not going to create a payment failure email for the business user type " +
        "because we are not dealing with the business user type")
      None
    }

    Seq(keeperEmail, businessEmail).flatten.toList
  }

  private def buildPaymentSuccessEmailRequests(captureCertificateDetails: CaptureCertificateDetailsModel,
                                               vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                                               transactionId: String,
                                               trackingId: TrackingId)
                                              (implicit request: Request[_]): List[EmailServiceSendRequest] = {

    val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]

    val businessDetails = vehicleAndKeeperLookupFormModel.userType match {
      case UserType_Business =>
        businessDetailsModel.map(model =>
          ReceiptEmailMessageBuilder.BusinessDetails(model.name, model.contact, model.address.address))
      case _ => None
    }

    val template = ReceiptEmailMessageBuilder.buildWith(
      vehicleAndKeeperLookupFormModel.replacementVRN,
      f"${captureCertificateDetails.outstandingFees.toDouble / 100}%.2f",
      transactionId,
      businessDetails)

    val title = s"Payment Receipt for assignment of ${vehicleAndKeeperLookupFormModel.replacementVRN}"

    val from = From(config.emailConfiguration.from.email, config.emailConfiguration.from.name)

    val isKeeperUserType = vehicleAndKeeperLookupFormModel.isKeeperUserType
    logMessage(trackingId, Debug, s"isKeeperUserType = $isKeeperUserType")

    val confirmFormModel = request.cookies.getModel[ConfirmFormModel]

    // send keeper email if present
    val keeperEmail: Option[EmailServiceSendRequest] = if (isKeeperUserType) {
      for {
        model <- confirmFormModel
        email <- model.keeperEmail
      } yield {
        logMessage(trackingId, Debug, "We are going to create a payment success email for the keeper user type")
        buildEmailServiceSendRequest(template, from, title, email)
      }
    } else {
      logMessage(trackingId, Debug, "We are not going to create a payment success email for the keeper user type " +
        "because we are not dealing with the keeper user type")
      None
    }

    if (keeperEmail.isEmpty &&
      isKeeperUserType &&
      confirmFormModel.nonEmpty &&
      confirmFormModel.get.keeperEmail.isEmpty
    ) {
      logMessage(trackingId, Debug, "We are not going to create a payment success email for the keeper user type " +
        "because no email was supplied")
    }

    val isBusinessUserType = vehicleAndKeeperLookupFormModel.isBusinessUserType
    logMessage(trackingId, Debug, s"isBusinessUserType = $isBusinessUserType")

    // send business email if present
    val businessEmail: Option[EmailServiceSendRequest] = if (isBusinessUserType) {
      for {
        model <- businessDetailsModel
      } yield {
        logMessage(trackingId, Debug, "We are going to create a payment success email for the business user type")
        buildEmailServiceSendRequest(template, from, title, model.email)
      }
    } else {
      logMessage(trackingId, Debug, "We are not going to create a payment success email for the business user type " +
        "because we are not dealing with the business user type")
      None
    }

    Seq(keeperEmail, businessEmail).flatten.toList
  }

  private def buildEmailServiceSendRequest(template: Contents, from: From, title: String, email: String) = {
    EmailServiceSendRequest(
      plainTextMessage = template.plainMessage,
      htmlMessage = template.htmlMessage,
      attachment = None,
      from = from,
      subject = title,
      toReceivers = Some(List(email)),
      ccReceivers = None
    )
  }
}
