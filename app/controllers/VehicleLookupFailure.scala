package controllers

import com.google.inject.Inject
import models._
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import utils.helpers.Config
import views.vrm_assign.VehicleLookup._

final class VehicleLookupFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                             config: Config) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[BruteForcePreventionModel],
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getString(VehicleAndKeeperLookupResponseCodeCacheKey)) match {
      case (Some(transactionId), Some(bruteForcePreventionResponse), Some(vehicleAndKeeperLookupForm),
      Some(vehicleLookupResponseCode)) =>
        val vehicleAndKeeperDetails = request.cookies.getModel[VehicleAndKeeperDetailsModel]
        displayVehicleLookupFailure(transactionId, vehicleAndKeeperLookupForm, bruteForcePreventionResponse,
          vehicleAndKeeperDetails, vehicleLookupResponseCode)
      case _ => Redirect(routes.BeforeYouStart.present())
    }
  }

  def submit = Action { implicit request =>
    request.cookies.getModel[VehicleAndKeeperLookupFormModel] match {
      case (Some(vehicleAndKeeperLookupFormModel)) =>
        Redirect(routes.VehicleLookup.present())
      case _ => Redirect(routes.BeforeYouStart.present())
    }
  }

  private def displayVehicleLookupFailure(transactionId: String,
                                          vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
                                          bruteForcePreventionModel: BruteForcePreventionModel,
                                          vehicleAndKeeperDetails: Option[VehicleAndKeeperDetailsModel],
                                          vehicleAndKeeperLookupResponseCode: String)(implicit request: Request[AnyContent]) = {
    val viewModel = vehicleAndKeeperDetails match {
      case Some(details) => VehicleLookupFailureViewModel(details)
      case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm)
    }

    Ok(views.html.vrm_assign.vehicle_lookup_failure(
      transactionId = transactionId,
      vehicleLookupFailureViewModel = viewModel,
      data = vehicleAndKeeperLookupForm,
      responseCodeVehicleLookupMSErrorMessage = vehicleAndKeeperLookupResponseCode,
      attempts = bruteForcePreventionModel.attempts,
      maxAttempts = bruteForcePreventionModel.maxAttempts,
      captureCertificateDetailsFormModel = request.cookies.getModel[CaptureCertificateDetailsFormModel],
      captureCertificateDetailsModel = request.cookies.getModel[CaptureCertificateDetailsModel]
    )
    ).discardingCookies(DiscardingCookie(name = VehicleAndKeeperLookupResponseCodeCacheKey))
  }
}