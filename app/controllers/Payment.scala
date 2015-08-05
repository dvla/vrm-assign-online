package controllers

import com.google.inject.Inject
import composition.RefererFromHeader
import models.BusinessDetailsModel
import models.CacheKeyPrefix
import models.CaptureCertificateDetailsModel
import models.CaptureCertificateDetailsFormModel
import models.ConfirmFormModel
import models.FulfilModel
import models.PaymentModel
import models.VehicleAndKeeperLookupFormModel
import org.apache.commons.codec.binary.Base64
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.Result
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_assign.Confirm.GranteeConsentCacheKey
import views.vrm_assign.Payment.PaymentTransNoCacheKey
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest
import webserviceclients.paymentsolve.PaymentSolveBeginRequest
import webserviceclients.paymentsolve.PaymentSolveCancelRequest
import webserviceclients.paymentsolve.PaymentSolveGetRequest
import webserviceclients.paymentsolve.PaymentSolveService

final class Payment @Inject()(paymentSolveService: PaymentSolveService,
                               refererFromHeader: RefererFromHeader,
                               auditService2: audit2.AuditService
                               )
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config,
                              dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService)
                            extends Controller with DVLALogger {

  def begin = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[FulfilModel],
      request.cookies.getString(GranteeConsentCacheKey)) match {
      case (Some(transactionId), Some(vehicleAndKeeperLookupFormModel), None, Some(granteeConsent))
        if granteeConsent == "true" =>
        callBeginWebPaymentService(transactionId, vehicleAndKeeperLookupFormModel.replacementVRN)
      case _ => Future.successful {
        logMessage( request.cookies.trackingId(), Warn, "Payment present failed matching cookies")
        Redirect(routes.ConfirmPayment.present())
      }
    }
  }

  // The token is checked in the common project, we do nothing with it here.
  def callback(token: String) = Action.async { implicit request =>
    // check whether it is past the closing time
    if (new DateTime(dateService.now, DateTimeZone.forID("Europe/London")).getHourOfDay >= config.closing)
      (request.cookies.getString(TransactionIdCacheKey), request.cookies.getModel[PaymentModel]) match {
        case (Some(transactionId), Some(paymentDetails)) =>
          callCancelWebPaymentService(transactionId, paymentDetails.trxRef.get, paymentDetails.isPrimaryUrl).map { _ =>
            Redirect(routes.PaymentPostShutdown.present())
          }
        case _ =>
          Future.successful(Redirect(routes.PaymentPostShutdown.present()))
      }
    else
      Future.successful(Redirect(routes.Payment.getWebPayment()))
  }

  def getWebPayment = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey), request.cookies.getModel[PaymentModel]) match {
      case (Some(transactionId), Some(paymentModel)) =>
        callGetWebPaymentService(transactionId, paymentModel.trxRef.get, isPrimaryUrl = paymentModel.isPrimaryUrl)
      case _ => Future.successful {
        paymentFailure("Payment getWebPayment missing TransactionIdCacheKey or PaymentTransactionReferenceCacheKey cookie")
      }
    }
  }

  def cancel = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey), request.cookies.getModel[PaymentModel]) match {
      case (Some(transactionId), Some(paymentModel)) =>

        val captureCertificateDetailsFormModel = request.cookies.getModel[CaptureCertificateDetailsFormModel].get
        val captureCertificateDetails = request.cookies.getModel[CaptureCertificateDetailsModel].get

        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.PaymentToExit,
          transactionId = transactionId,
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
          captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
          captureCertificateDetailsModel = Some(captureCertificateDetails),
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

        Future.successful {
          redirectToLeaveFeedback
        }

      case (transactionId, paymentModel) => Future.successful {
        paymentFailure(s"Payment cancel missing either TransactionIdCacheKey: $transactionId or paymentModel $paymentModel cookie")
      }
    }
  }

  private def paymentFailure(message: String)(implicit request: Request[_]) = {
    logMessage( request.cookies.trackingId(), Error, message)

    val captureCertificateDetailsFormModel = request.cookies.getModel[CaptureCertificateDetailsFormModel]
    val captureCertificateDetails = request.cookies.getModel[CaptureCertificateDetailsModel]

    auditService2.send(AuditRequest.from(
      pageMovement = AuditRequest.PaymentToPaymentFailure,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
      paymentModel = request.cookies.getModel[PaymentModel],
      captureCertificateDetailFormModel = captureCertificateDetailsFormModel,
      captureCertificateDetailsModel = captureCertificateDetails,
      rejectionCode = Some(message)))

    Redirect(routes.PaymentFailure.present())
  }

  private def callBeginWebPaymentService(transactionId: String, vrm: String)
                                        (implicit request: Request[_],
                                         token: uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.CsrfPreventionToken): Future[Result] = {

    refererFromHeader.fetch match {
      case Some(referer) =>
        val tokenBase64URLSafe = Base64.encodeBase64URLSafeString(token.value.getBytes)
        val paymentCallback = refererFromHeader.paymentCallbackUrl(referer = referer, tokenBase64URLSafe = tokenBase64URLSafe)
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

        paymentSolveService.invoke(paymentSolveBeginRequest, trackingId).map { response =>
          if (response.status == Payment.CardDetailsStatus) {
            Ok(views.html.vrm_assign.payment(paymentRedirectUrl = response.redirectUrl.get))
              .withCookie(PaymentModel.from(response.trxRef.get, isPrimaryUrl = response.isPrimaryUrl))
              .withCookie(REFERER, routes.Payment.begin().url) // The POST from payment service will not contain a REFERER in the header, so use a cookie.
          } else {
            paymentFailure(s"The begin web request to Solve was not validated. Payment Solve encountered a problem with request ${LogFormats.anonymize(vrm)}, redirect to PaymentFailure")
          }
        }.recover {
          case NonFatal(e) =>
            paymentFailure(message = s"Payment Solve web service call with paymentSolveBeginRequest failed. Exception " + e.toString)
        }
      case _ => Future.successful(paymentFailure(message = "Payment callBeginWebPaymentService no referer"))
    }
  }

  private def callGetWebPaymentService(transactionId: String, trxRef: String, isPrimaryUrl: Boolean)
                                      (implicit request: Request[_]): Future[Result] = {

    def paymentNotAuthorised = {
      logMessage( request.cookies.trackingId(), Debug, s"Payment not authorised for ${LogFormats.anonymize(trxRef)}, redirect to PaymentNotAuthorised")

      val paymentModel = request.cookies.getModel[PaymentModel].get
      val captureCertificateDetailsFormModel = request.cookies.getModel[CaptureCertificateDetailsFormModel].get
      val captureCertificateDetails = request.cookies.getModel[CaptureCertificateDetailsModel].get

      auditService2.send(AuditRequest.from(
        pageMovement = AuditRequest.PaymentToPaymentNotAuthorised,
        transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
        captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
        captureCertificateDetailsModel = Some(captureCertificateDetails),
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
        paymentModel = Some(paymentModel)))

      Redirect(routes.PaymentNotAuthorised.present())
        .withCookie(paymentModel)
    }

    val transNo = request.cookies.getString(PaymentTransNoCacheKey).get

    val paymentSolveGetRequest = PaymentSolveGetRequest(
      transNo = transNo,
      trxRef = trxRef,
      isPrimaryUrl = isPrimaryUrl
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveGetRequest, trackingId).map { response =>
      if (response.status == Payment.AuthorisedStatus) {

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
      } else {
        logMessage( request.cookies.trackingId(), Debug, "The payment was not authorised.")
        paymentNotAuthorised
      }
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
      if (response.response == Payment.CancelledStatus) {
        logMessage( trackingId, Error, "The get web request to Solve was not validated.")
      }

      val captureCertificateDetailsFormModel = request.cookies.getModel[CaptureCertificateDetailsFormModel].get
      val captureCertificateDetails = request.cookies.getModel[CaptureCertificateDetailsModel].get

      auditService2.send(AuditRequest.from(
        pageMovement = AuditRequest.PaymentToExit,
        transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
        captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
        captureCertificateDetailsModel = Some(captureCertificateDetails),
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

      redirectToLeaveFeedback
    }.recover {
      case NonFatal(e) =>
        logMessage( trackingId, Error, s"Payment Solve web service call with paymentSolveCancelRequest failed. Exception ${e.getMessage}")
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