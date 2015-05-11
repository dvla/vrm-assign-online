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

      case (Some(transactionId), paymentModelOpt, vehicleAndKeeperDetails, Some(vehicleAndKeeperLookupForm),
        captureCertificateDetailsFormModel, captureCertificateDetailsModel) => {
        val viewModel = vehicleAndKeeperDetails match {
          case Some(details) => VehicleLookupFailureViewModel(details)
          case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm)
        }
        Future.successful(Ok(views.html.vrm_assign.fulfil_failure(transactionId, paymentModelOpt.isDefined, viewModel,
          captureCertificateDetailsFormModel, captureCertificateDetailsModel)))
      }
      case _ =>
        Future.successful(Redirect(routes.Error.present("user tried to go to FulfilFailure but the required cookie was not present")))
    }
  }

}