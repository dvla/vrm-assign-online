package controllers

import com.google.inject.Inject
import models._
import play.api.Logger
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.html.vrm_assign.lookup_failure.cert_number_mismatch
import views.html.vrm_assign.lookup_failure.direct_to_paper
import views.html.vrm_assign.lookup_failure.eligibility
import views.html.vrm_assign.lookup_failure.vehicle_lookup_failure
import views.vrm_assign.VehicleLookup._

final class VehicleLookupFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                             config: Config,
                                             dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[BruteForcePreventionModel],
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getString(VehicleAndKeeperLookupResponseCodeCacheKey),
      request.cookies.getModel[CaptureCertificateDetailsFormModel],
      request.cookies.getModel[CaptureCertificateDetailsModel]) match {
      case (Some(transactionId), Some(bruteForcePreventionResponse), Some(vehicleAndKeeperLookupForm),
      Some(vehicleLookupResponseCode), captureCertificateDetailsFormModel, captureCertificateDetailsModel) =>
        val vehicleAndKeeperDetails = request.cookies.getModel[VehicleAndKeeperDetailsModel]
        displayVehicleLookupFailure(transactionId, vehicleAndKeeperLookupForm, bruteForcePreventionResponse,
          vehicleAndKeeperDetails, vehicleLookupResponseCode, captureCertificateDetailsFormModel, captureCertificateDetailsModel)
      case _ =>
        Logger.warn("*** VehicleLookupFailure present cookie missing go to BeforeYouStart")
        Redirect(routes.BeforeYouStart.present())
    }
  }

  def submit = Action { implicit request =>
    request.cookies.getModel[VehicleAndKeeperLookupFormModel] match {
      case (Some(vehicleAndKeeperLookupFormModel)) =>
        Redirect(routes.VehicleLookup.present())
      case _ => Redirect(routes.BeforeYouStart.present())
    }
  }

  def tryAgain = Action { implicit request =>
    Redirect(routes.VehicleLookup.present())
  }

  private def displayVehicleLookupFailure(transactionId: String,
                                          vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
                                          bruteForcePreventionModel: BruteForcePreventionModel,
                                          vehicleAndKeeperDetails: Option[VehicleAndKeeperDetailsModel],
                                          vehicleAndKeeperLookupResponseCode: String,
                                          captureCertificateDetailsFormModel: Option[CaptureCertificateDetailsFormModel],
                                          captureCertificateDetailsModel: Option[CaptureCertificateDetailsModel])(implicit request: Request[AnyContent]) = {
    val viewModel = vehicleAndKeeperDetails match {
      case Some(details) => VehicleLookupFailureViewModel(details)
      case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm)
    }

    val failurePage = vehicleAndKeeperLookupResponseCode match {
      case "vrm_assign_eligibility_direct_to_paper" =>
        direct_to_paper(
          transactionId = transactionId,
          viewModel = viewModel,
          vehicleAndKeeperLookupFormModel = vehicleAndKeeperLookupForm,
          captureCertificateDetailsModel = captureCertificateDetailsModel
        )
      case "vrm_assign_eligibility_cert_number_mismatch" =>
        cert_number_mismatch(
          transactionId = transactionId,
          viewModel = viewModel,
          vehicleAndKeeperLookupFormModel = vehicleAndKeeperLookupForm
        )
      case "vrm_assign_eligibility_failure" =>
        eligibility(
          transactionId = transactionId,
          viewModel = viewModel,
          vehicleAndKeeperLookupFormModel = vehicleAndKeeperLookupForm

        )
      case _ =>
        vehicle_lookup_failure(
          transactionId = transactionId,
          viewModel = viewModel,
          responseCodeVehicleLookupMSErrorMessage = vehicleAndKeeperLookupResponseCode,
          vehicleAndKeeperLookupFormModel = vehicleAndKeeperLookupForm
        )
    }

    Ok(failurePage).discardingCookies(DiscardingCookie(name = VehicleAndKeeperLookupResponseCodeCacheKey))
  }
}