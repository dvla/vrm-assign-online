package controllers

import com.google.inject.Inject
import models.PaymentModel
import play.api.Logger
import play.api.mvc.Result
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import utils.helpers.Config
import views.vrm_assign.VehicleLookup._
import webserviceclients.paymentsolve.PaymentSolveCancelRequest
import webserviceclients.paymentsolve.PaymentSolveService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class FulfilFailure @Inject()(paymentSolveService: PaymentSolveService)
                                   (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config,
                                    dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  def present = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[PaymentModel]) match {

      case (Some(transactionId), Some(paymentModel)) =>
        callCancelWebPaymentService(transactionId, paymentModel.trxRef.get)
      case (Some(transactionId), None) =>
        Future.successful(Ok(views.html.vrm_assign.fulfil_failure(false)))
      case _ =>
        Future.successful(Redirect(routes.Error.present("user tried to go to FulfilFailure but the required cookie was not present")))
    }
  }

  private def callCancelWebPaymentService(transactionId: String, trxRef: String)
                                         (implicit request: Request[_]): Future[Result] = {

    val paymentSolveCancelRequest = PaymentSolveCancelRequest(
      transNo = transactionId.replaceAll("[^0-9]", ""),
      trxRef = trxRef
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveCancelRequest, trackingId).map {
      response =>
        Ok(views.html.vrm_assign.fulfil_failure(true))
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"FulfilFailure Payment Solve web service call with paymentSolveCancelRequest failed. Exception " + e.toString)
        Ok(views.html.vrm_assign.fulfil_failure(true))
    }
  }
}