package controllers

import com.google.inject.Inject
import models._
import play.api.Logger
import play.api.mvc.Result
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
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
      request.cookies.getModel[PaymentModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[CaptureCertificateDetailsFormModel],
      request.cookies.getModel[CaptureCertificateDetailsModel]) match {

      case (Some(transactionId), Some(paymentModel), vehicleAndKeeperDetails, Some(vehicleAndKeeperLookupForm), captureCertificateDetailsFormModel, captureCertificateDetailsModel) =>
        callCancelWebPaymentService(transactionId, paymentModel.trxRef.get, paymentModel.isPrimaryUrl, vehicleAndKeeperDetails, vehicleAndKeeperLookupForm, captureCertificateDetailsFormModel, captureCertificateDetailsModel)
      case (Some(transactionId), None, vehicleAndKeeperDetails, Some(vehicleAndKeeperLookupForm), captureCertificateDetailsFormModel, captureCertificateDetailsModel) =>
        val viewModel = vehicleAndKeeperDetails match {
          case Some(details) => VehicleLookupFailureViewModel(details)
          case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm)
        }
        Future.successful(Ok(views.html.vrm_assign.fulfil_failure(transactionId, false, viewModel, captureCertificateDetailsFormModel, captureCertificateDetailsModel)))
      case _ =>
        Future.successful(Redirect(routes.Error.present("user tried to go to FulfilFailure but the required cookie was not present")))
    }
  }

  private def callCancelWebPaymentService(
                                           transactionId: String,
                                           trxRef: String,
                                           isPrimaryUrl: Boolean,
                                           vehicleAndKeeperDetails: Option[VehicleAndKeeperDetailsModel],
                                           vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
                                           captureCertificateDetailsFormModel: Option[CaptureCertificateDetailsFormModel],
                                           captureCertificateDetailsModel: Option[CaptureCertificateDetailsModel])
                                         (implicit request: Request[_]): Future[Result] = {

    val paymentSolveCancelRequest = PaymentSolveCancelRequest(
      transNo = transactionId.replaceAll("[^0-9]", ""),
      trxRef = trxRef,
      isPrimaryUrl = isPrimaryUrl
    )
    val trackingId = request.cookies.trackingId()

    val viewModel = vehicleAndKeeperDetails match {
      case Some(details) => VehicleLookupFailureViewModel(details)
      case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm)
    }

    paymentSolveService.invoke(paymentSolveCancelRequest, trackingId).map {
      response =>
        Ok(views.html.vrm_assign.fulfil_failure(transactionId, true, viewModel, captureCertificateDetailsFormModel, captureCertificateDetailsModel))
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"FulfilFailure Payment Solve web service call with paymentSolveCancelRequest failed. Exception " + e.toString)
        Ok(views.html.vrm_assign.fulfil_failure(transactionId, true, viewModel, captureCertificateDetailsFormModel, captureCertificateDetailsModel))
    }
  }
}