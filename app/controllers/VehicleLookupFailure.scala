package controllers

import com.google.inject.Inject
import mappings.common.ErrorCodes
import models.CacheKeyPrefix
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.DirectToPaperViewModel
import models.VehicleAndKeeperLookupFormModel
import models.VehicleLookupFailureViewModel
import play.api.mvc.{Action, AnyContent, Controller, DiscardingCookie, Request}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import uk.gov.dvla.vehicles.presentation.common.model.MicroserviceResponseModel
import uk.gov.dvla.vehicles.presentation.common.model.MicroserviceResponseModel.MsResponseCacheKey
import utils.helpers.Config
import views.html.vrm_assign.lookup_failure.cert_expired
import views.html.vrm_assign.lookup_failure.cert_number_mismatch
import views.html.vrm_assign.lookup_failure.direct_to_paper
import views.html.vrm_assign.lookup_failure.eligibility
import views.html.vrm_assign.lookup_failure.vehicle_lookup_failure
import views.html.vrm_assign.lookup_failure.postcode_mismatch
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey

final class VehicleLookupFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                             config: Config,
                                             dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService)
                                            extends Controller with DVLALogger {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[BruteForcePreventionModel],
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[MicroserviceResponseModel],
      request.cookies.getModel[CaptureCertificateDetailsFormModel],
      request.cookies.getModel[CaptureCertificateDetailsModel]) match {
      case (Some(transactionId),
            Some(bruteForcePreventionResponse),
            Some(vehicleAndKeeperLookupForm),
            Some(msResponseModel),
            captureCertificateDetailsFormModel,
            captureCertificateDetailsModel) =>
        displayVehicleLookupFailure(transactionId,
          vehicleAndKeeperLookupForm,
          bruteForcePreventionResponse,
          msResponseModel,
          captureCertificateDetailsFormModel,
          captureCertificateDetailsModel
        )
      case _ =>
        logMessage(request.cookies.trackingId(), Warn,
          "VehicleLookupFailure present cookie missing, redirecting to BeforeYouStart")
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
                                          msResponseModel: MicroserviceResponseModel,
                                          captureCertificateDetailsFormModel: Option[CaptureCertificateDetailsFormModel],
                                          captureCertificateDetailsModel: Option[CaptureCertificateDetailsModel])
                                         (implicit request: Request[AnyContent]) = {

    val viewModel = VehicleLookupFailureViewModel(
      vehicleAndKeeperLookupForm,
      msResponseModel.msResponse.code
    )

    val intro = "VehicleLookupFailure is"
    val failurePage = msResponseModel.msResponse.message match {
      case "vrm_assign_eligibility_direct_to_paper" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting direct to paper failure view")
        direct_to_paper(
          transactionId = transactionId,
          viewModel = DirectToPaperViewModel.from(viewModel, captureCertificateDetailsModel)
        )
      case "vrm_assign_eligibility_cert_number_mismatch" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting cert number mismatch failure view")
        cert_number_mismatch(
          transactionId = transactionId,
          viewModel = viewModel
        )
      case "vrm_assign_eligibility_cert_expired" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting cert expired failure view")
        cert_expired(
          transactionId = transactionId,
          viewModel = viewModel
        )
      case "vrm_assign_eligibility_failure" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting eligibility failure view")
        eligibility(
          transactionId = transactionId,
          viewModel = viewModel
        )
      case "vrm_assign_eligibility_cert_begin_failure" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting cert number mismatch failure view")
        cert_number_mismatch(
          transactionId = transactionId,
          viewModel = viewModel
        )
      case "vrm_assign_eligibility_q_plate_failure" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting eligibility failure view")
        eligibility(
          transactionId = transactionId,
          viewModel = viewModel,
          responseMessage = Some("vrm_assign_eligibility_q_plate_failure")
        )
      case "vrm_assign_eligibility_v778_failure" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting eligibility failure view")
        eligibility(
          transactionId = transactionId,
          viewModel = viewModel,
          responseMessage = Some("vrm_assign_eligibility_v778_failure")
        )
      case "vrm_assign_eligibility_vrm_failure" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting eligibility failure view")
        eligibility(
          transactionId = transactionId,
          viewModel = viewModel,
          responseMessage = Some("vrm_assign_eligibility_vrm_failure")
        )
      case "vrm_assign_eligibility_no_keeper_failure" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting direct to paper failure view")
        direct_to_paper(
          transactionId = transactionId,
          viewModel = DirectToPaperViewModel.from(viewModel, captureCertificateDetailsModel),
          responseMessage = Some("vrm_assign_eligibility_no_keeper_failure"),
          responseLink = Some("vrm_assign_eligibility_no_keeper_failure_link")
        )
      case "vrm_assign_eligibility_exported_failure" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting eligibility failure view")
        eligibility(
          transactionId = transactionId,
          viewModel = viewModel,
          responseMessage = Some("vrm_assign_eligibility_exported_failure")
        )
      case "vrm_assign_eligibility_scrapped_failure" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting eligibility failure view")
        eligibility(
          transactionId = transactionId,
          viewModel = viewModel,
          responseMessage = Some("vrm_assign_eligibility_scrapped_failure")
        )
      case "vrm_assign_eligibility_damaged_failure" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting direct to paper failure view")
        direct_to_paper(
          transactionId = transactionId,
          viewModel = DirectToPaperViewModel.from(viewModel, captureCertificateDetailsModel),
          responseMessage = Some("vrm_assign_eligibility_damaged_failure")
        )
      case "vrm_assign_eligibility_vic_failure" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting direct to paper failure view")
        direct_to_paper(
          transactionId = transactionId,
          viewModel = DirectToPaperViewModel.from(viewModel, captureCertificateDetailsModel),
          responseMessage = Some("vrm_assign_eligibility_vic_failure"),
          responseLink = Some("vrm_assign_eligibility_vic_failure_link")
        )
      case "vrm_assign_eligibility_destroyed_failure" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting direct to paper failure view")
        direct_to_paper(
          transactionId = transactionId,
          viewModel = DirectToPaperViewModel.from(viewModel, captureCertificateDetailsModel),
          responseMessage = Some("vrm_assign_eligibility_destroyed_failure")
        )
      case "vrm_assign_eligibility_not_mot_failure" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting eligibility failure view")
        eligibility(
          transactionId = transactionId,
          viewModel = viewModel,
          responseMessage = Some("vrm_assign_eligibility_not_mot_failure"),
          responseLink = Some("vrm_assign_eligibility_not_mot_failure_link")
        )
      case "vrm_assign_eligibility_too_young_failure" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting eligibility failure view")
        eligibility(
          transactionId = transactionId,
          viewModel = viewModel,
          responseMessage = Some("vrm_assign_eligibility_too_young_failure")
        )
      case "vrm_assign_eligibility_v778_ref_failure" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting cert number mismatch failure view")
        cert_number_mismatch(
          transactionId = transactionId,
          viewModel = viewModel
        )
      case "vrm_assign_eligibility_ninety_day_rule_failure" =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting ninety day rule failure view")
        direct_to_paper(
          transactionId = transactionId,
          viewModel = DirectToPaperViewModel.from(viewModel, captureCertificateDetailsModel),
          responseMessage = Some("vrm_assign_eligibility_ninety_day_rule_failure")
        )
      case VehicleLookupBase.RESPONSE_CODE_POSTCODE_MISMATCH =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting postcode mismatch view")
        postcode_mismatch(
          transactionId = transactionId,
          viewModel = viewModel.copy(failureCode = ErrorCodes.PostcodeMismatchErrorCode)
        )
      case _ =>
        logMessage(request.cookies.trackingId(), Info, s"$intro presenting vehicle lookup failure view")
        vehicle_lookup_failure(
          transactionId = transactionId,
          viewModel = viewModel,
          responseCodeVehicleLookupMSErrorMessage = msResponseModel.msResponse.message
        )
    }
    Ok(failurePage).discardingCookies(DiscardingCookie(name = MsResponseCacheKey))
  }
}
