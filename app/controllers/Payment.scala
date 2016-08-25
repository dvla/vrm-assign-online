package controllers

import com.google.inject.Inject
import composition.RefererFromHeader
import models.{BusinessDetailsModel, CacheKeyPrefix, CaptureCertificateDetailsFormModel, CaptureCertificateDetailsModel, ConfirmFormModel, FulfilModel, PaymentModel, VehicleAndKeeperLookupFormModel}
import org.apache.commons.codec.binary.Base64
import play.api.mvc.{Action, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common
import common.LogFormats.{DVLALogger, anonymize}
import common.clientsidesession.{ClearTextClientSideSessionFactory, ClientSideSessionFactory}
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.filters.CsrfPreventionAction.CsrfPreventionToken
import common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_assign.Confirm.GranteeConsentCacheKey
import views.vrm_assign.Payment.PaymentTransNoCacheKey
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey
import webserviceclients.audit2.AuditRequest
import webserviceclients.paymentsolve.{PaymentSolveBeginRequest, PaymentSolveCancelRequest, PaymentSolveGetRequest, PaymentSolveService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class Payment @Inject()(paymentSolveService: PaymentSolveService,
                               refererFromHeader: RefererFromHeader,
                               auditService2: webserviceclients.audit2.AuditService)
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config,
                              dateService: common.services.DateService) extends Controller with DVLALogger {

  def begin = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[FulfilModel],
      request.cookies.getString(GranteeConsentCacheKey)) match {
      case (Some(transactionId), Some(vehicleAndKeeperLookupFormModel), None, Some(granteeConsent))
        if granteeConsent == "true" =>
        callBeginWebPaymentService(transactionId, vehicleAndKeeperLookupFormModel.replacementVRN)
      case _ => Future.successful {
        logMessage(request.cookies.trackingId(), Warn, "Payment present failed matching cookies")
        Redirect(routes.ConfirmPayment.present())
      }
    }
  }

  // The token is checked in the common project, we do nothing with it here.
  def callback(token: String) = Action.async { implicit request =>
    Future.successful(Redirect(routes.Payment.getWebPayment()))
  }

  def getWebPayment = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey), request.cookies.getModel[PaymentModel]) match {
      case (Some(transactionId), Some(paymentModel)) =>
        callGetWebPaymentService(transactionId, paymentModel.trxRef.get, isPrimaryUrl = paymentModel.isPrimaryUrl)
      case _ => Future.successful {
        paymentFailure(
          "Payment getWebPayment missing TransactionIdCacheKey or PaymentTransactionReferenceCacheKey cookie"
        )
      }
    }
  }

  def cancel = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey), request.cookies.getModel[PaymentModel]) match {
      case (Some(transactionId), Some(paymentModel)) =>
        val captureCertificateDetailsFormModel = request.cookies.getModel[CaptureCertificateDetailsFormModel].get
        val captureCertificateDetails = request.cookies.getModel[CaptureCertificateDetailsModel].get
        val trackingId = request.cookies.trackingId()
        auditService2.send(
          AuditRequest.from(
            pageMovement = AuditRequest.PaymentToExit,
            transactionId = transactionId,
            timestamp = dateService.dateTimeISOChronology,
            documentReferenceNumber = request.cookies.getModel[VehicleAndKeeperLookupFormModel].map(_.referenceNumber),
            vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
            keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
            captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
            captureCertificateDetailsModel = Some(captureCertificateDetails),
            businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]
          ), trackingId
        )

        Future.successful {
          redirectToLeaveFeedback
        }
      case (transactionId, paymentModel) => Future.successful {
        paymentFailure("Payment cancel missing TransactionIdCacheKey: " +
          s"$transactionId or paymentModel $paymentModel cookie"
        )
      }
    }
  }

  private def paymentFailure(message: String)(implicit request: Request[_]) = {
    logMessage(request.cookies.trackingId(),Error, message)

    val captureCertificateDetailsFormModel = request.cookies.getModel[CaptureCertificateDetailsFormModel]
    val captureCertificateDetails = request.cookies.getModel[CaptureCertificateDetailsModel]
    val trackingId = request.cookies.trackingId()
    auditService2.send(
      AuditRequest.from(
        pageMovement = AuditRequest.PaymentToPaymentFailure,
        transactionId = request.cookies.getString(TransactionIdCacheKey)
          .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
        timestamp = dateService.dateTimeISOChronology,
        documentReferenceNumber = request.cookies.getModel[VehicleAndKeeperLookupFormModel].map(_.referenceNumber),
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
        paymentModel = request.cookies.getModel[PaymentModel],
        captureCertificateDetailFormModel = captureCertificateDetailsFormModel,
        captureCertificateDetailsModel = captureCertificateDetails,
        rejectionCode = Some(message)
      ), trackingId
    )

    Redirect(routes.PaymentFailure.present())
  }

  private def callBeginWebPaymentService(transactionId: String, vrm: String)
                                        (implicit request: Request[_],
                                         token: CsrfPreventionToken): Future[Result] = {
    refererFromHeader.fetch match {
      case Some(referer) =>
        val tokenBase64URLSafe = Base64.encodeBase64URLSafeString(token.value.getBytes)
        val paymentCallback = refererFromHeader.paymentCallbackUrl(
          referer = referer,
          tokenBase64URLSafe = tokenBase64URLSafe
        )
        val transNo = request.cookies.getString(PaymentTransNoCacheKey).get
        val outstandingFees = request.cookies.getModel[CaptureCertificateDetailsModel].get.outstandingFees
        val paymentSolveBeginRequest = PaymentSolveBeginRequest(
          transactionId = transactionId,
          transNo = transNo,
          vrm = vrm,
          purchaseAmount = outstandingFees,
          paymentCallback = paymentCallback
        )
        val trackingId = request.cookies.trackingId()

        paymentSolveService.invoke(paymentSolveBeginRequest, trackingId).map {
          case (OK, response) if response.beginResponse.status == Payment.CardDetailsStatus =>
            logMessage(request.cookies.trackingId(), Info, s"Presenting payment view")
            Ok(views.html.vrm_assign.payment (paymentRedirectUrl = response.redirectUrl.get))
              .withCookie(PaymentModel.from(trxRef = response.trxRef.get, isPrimaryUrl = response.isPrimaryUrl))
              // The POST from payment service will not contain a REFERER in the header, so use a cookie.
              .withCookie(REFERER, routes.Payment.begin().url)
          case (_, response) =>
            paymentFailure(s"The begin web request to Solve encountered a problem with request " +
              s"${anonymize(vrm)}, response: ${response.beginResponse.response}, " +
              s"status: ${response.beginResponse.status}, redirect to PaymentFailure")
        }.recover {
          case NonFatal(e) =>
            paymentFailure(
              message = s"Payment Solve web service call with paymentSolveBeginRequest failed. Exception " + e.toString
            )
        }
      case _ => Future.successful(paymentFailure(message = "Payment callBeginWebPaymentService no referer"))
    }
  }

  private def callGetWebPaymentService(transactionId: String, trxRef: String, isPrimaryUrl: Boolean)
                                      (implicit request: Request[_]): Future[Result] = {

    def paymentNotAuthorised = {
      logMessage(request.cookies.trackingId(), Debug,
        s"Payment not authorised for ${anonymize(trxRef)}, redirect to PaymentNotAuthorised")

      val paymentModel = request.cookies.getModel[PaymentModel].get
      val captureCertificateDetailsFormModel = request.cookies.getModel[CaptureCertificateDetailsFormModel].get
      val captureCertificateDetails = request.cookies.getModel[CaptureCertificateDetailsModel].get
      val trackingId = request.cookies.trackingId()
      auditService2.send(
        AuditRequest.from(
          pageMovement = AuditRequest.PaymentToPaymentNotAuthorised,
          transactionId = request.cookies.getString(TransactionIdCacheKey)
            .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
          timestamp = dateService.dateTimeISOChronology,
          documentReferenceNumber = request.cookies.getModel[VehicleAndKeeperLookupFormModel].map(_.referenceNumber),
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
          captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
          captureCertificateDetailsModel = Some(captureCertificateDetails),
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
          paymentModel = Some(paymentModel)
        ), trackingId
      )

      Redirect(routes.PaymentNotAuthorised.present()).withCookie(paymentModel)
    }

    val transNo = request.cookies.getString(PaymentTransNoCacheKey).get

    val paymentSolveGetRequest = PaymentSolveGetRequest(
      transNo = transNo,
      trxRef = trxRef,
      isPrimaryUrl = isPrimaryUrl
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveGetRequest, trackingId).map {
      case (OK, response) if response.getResponse.status == Payment.AuthorisedStatus =>
        val paymentModel = request.cookies.getModel[PaymentModel].get

        paymentModel.authCode = response.authcode
        paymentModel.maskedPAN = response.maskedPAN
        paymentModel.cardType = response.cardType
        paymentModel.merchantId = response.merchantTransactionId
        paymentModel.paymentType = response.paymentType
        paymentModel.totalAmountPaid = response.purchaseAmount
        paymentModel.paymentStatus = Some(Payment.AuthorisedStatus)

        Redirect(routes.Fulfil.fulfil())
          .discardingCookie(REFERER) // Not used again.
          .withCookie(paymentModel)
      case (_, response) =>
        logMessage(request.cookies.trackingId(), Error,
          "The payment was not authorised - " +
          s"response: ${response.getResponse.response}, status: ${response.getResponse.status}.")
        paymentNotAuthorised
    }.recover {
      case NonFatal(e) =>
        paymentFailure(message = "Payment Solve web service call with paymentSolveGetRequest failed: " + e.toString)
    }
  }

  private def callCancelWebPaymentService(transactionId: String, trxRef: String, isPrimaryUrl: Boolean)
                                         (implicit request: Request[_]): Future[Result] = {

    val transNo = request.cookies.getString(PaymentTransNoCacheKey).get

    val paymentSolveCancelRequest = PaymentSolveCancelRequest(
      transNo = transNo,
      trxRef = trxRef,
      isPrimaryUrl = isPrimaryUrl
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveCancelRequest, trackingId).map { response =>
      if (response._2.status == Payment.CancelledStatus) {
        logMessage(trackingId, Info, "The web request to Solve was cancelled.")
      } else {
        logMessage(trackingId, Error, "The cancel was not successful, " +
          s"response: ${response._2.response}, status: ${response._2.status}.")
      }

      val captureCertificateDetailsFormModel = request.cookies.getModel[CaptureCertificateDetailsFormModel].get
      val captureCertificateDetails = request.cookies.getModel[CaptureCertificateDetailsModel].get

      auditService2.send(
        AuditRequest.from(
          pageMovement = AuditRequest.PaymentToExit,
          transactionId = request.cookies.getString(TransactionIdCacheKey).
            getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
          timestamp = dateService.dateTimeISOChronology,
          documentReferenceNumber = request.cookies.getModel[VehicleAndKeeperLookupFormModel].map(_.referenceNumber),
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
          captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
          captureCertificateDetailsModel = Some(captureCertificateDetails),
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]
        ),
        trackingId
      )

      redirectToLeaveFeedback
    }.recover {
      case NonFatal(e) =>
        logMessage(
          trackingId,
          Error, 
          "Payment Solve web service call with paymentSolveCancelRequest failed.  " + s"Exception ${e.getMessage}")
        redirectToLeaveFeedback
    }
  }

  private def redirectToLeaveFeedback(implicit request: Request[_]) = {
    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }
}

object Payment {
  final val CardDetailsStatus = "CARD_DETAILS"
  final val AuthorisedStatus = "AUTHORISED"
  final val CancelledStatus = "CANCELLED"
  final val SettledStatus = "SETTLED"
}
