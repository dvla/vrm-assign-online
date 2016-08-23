package controllers

import com.google.inject.Inject
import models.CacheKeyPrefix
import models.VehicleAndKeeperLookupFormModel
import models.VehicleLookupFailureViewModel
import play.api.mvc.{Action, AnyContent, Controller, Request}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey

final class PaymentPostShutdown @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                            config: Config,
                                            dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService
                                            ) extends Controller with DVLALogger {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel]) match {
      case (Some(transactionId), Some(vehicleAndKeeperLookupForm)) =>
        val vehicleAndKeeperDetails = request.cookies.getModel[VehicleAndKeeperDetailsModel]
        displayPaymentPostShutdown(transactionId, vehicleAndKeeperLookupForm, vehicleAndKeeperDetails)
      case _ => Redirect(routes.BeforeYouStart.present())
    }
  }

  private def displayPaymentPostShutdown(transactionId: String,
                                         vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
                                         vehicleAndKeeperDetails: Option[VehicleAndKeeperDetailsModel]
                                          )(implicit request: Request[AnyContent]) = {
    val viewModel = vehicleAndKeeperDetails match {
      case Some(details) => VehicleLookupFailureViewModel(details, vehicleAndKeeperLookupForm, failureCode = "")
      case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm, failureCode = "")
    }

    logMessage(request.cookies.trackingId(), Info, s"Presenting payment post shutdown view")
    Ok(views.html.vrm_assign.payment_post_shutdown(
      transactionId = transactionId,
      vehicleLookupFailureViewModel = viewModel
    ))
  }
}
