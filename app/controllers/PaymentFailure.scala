package controllers

import com.google.inject.Inject
import models._
import play.api.Logger
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup._

final class PaymentFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config,
                                       dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[CaptureCertificateDetailsFormModel]) match {
      case (Some(transactionId), Some(vehicleAndKeeperLookupForm), Some(captureCertificateDetailsFormModel)) =>
        val vehicleAndKeeperDetails = request.cookies.getModel[VehicleAndKeeperDetailsModel]
        displayPaymentFailure(transactionId, vehicleAndKeeperLookupForm, vehicleAndKeeperDetails, captureCertificateDetailsFormModel)
      case _ =>
        Logger.warn("*** PaymentFailure present cookie missing go to BeforeYouStart")
        Redirect(routes.BeforeYouStart.present())
    }
  }

  def submit = Action { implicit request =>
    Redirect(routes.VehicleLookup.present()).
      discardingCookies(removeCookiesOnExit)
  }

  private def displayPaymentFailure(transactionId: String,
                                    vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
                                    vehicleAndKeeperDetails: Option[VehicleAndKeeperDetailsModel],
                                    captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel)
                                   (implicit request: Request[AnyContent]) = {
    val viewModel = vehicleAndKeeperDetails match {
      case Some(details) => VehicleLookupFailureViewModel(details)
      case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm)
    }

    Ok(views.html.vrm_assign.payment_failure(
      transactionId = transactionId,
      vehicleLookupFailureViewModel = viewModel,
      data = vehicleAndKeeperLookupForm,
      captureCertificateDetailsFormModel = captureCertificateDetailsFormModel)
    ).
      discardingCookies(removeCookiesOnExit)
  }
}