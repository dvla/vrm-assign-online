package controllers

import com.google.inject.Inject
import models.CacheKeyPrefix
import models.CaptureCertificateDetailsFormModel
import models.VehicleLookupFailureViewModel
import models.VehicleAndKeeperLookupFormModel
import play.api.mvc.{Action, AnyContent, Controller, Request}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey

final class PaymentFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config,
                                       dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService
                                      ) extends Controller with DVLALogger {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[CaptureCertificateDetailsFormModel]) match {
      case (Some(transactionId), Some(vehicleAndKeeperLookupForm), Some(captureCertificateDetailsFormModel)) =>
        val vehicleAndKeeperDetails = request.cookies.getModel[VehicleAndKeeperDetailsModel]
        displayPaymentFailure(transactionId,
          vehicleAndKeeperLookupForm,
          vehicleAndKeeperDetails,
          captureCertificateDetailsFormModel
        )
      case _ =>
        logMessage(request.cookies.trackingId, Warn, "PaymentFailure present cookie missing go to BeforeYouStart")
        Redirect(routes.BeforeYouStart.present())
    }
  }

  def submit = Action { implicit request =>
    Redirect(routes.VehicleLookup.present())
      .discardingCookies(removeCookiesOnExit)
  }

  private def displayPaymentFailure(transactionId: String,
                                    vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
                                    vehicleAndKeeperDetails: Option[VehicleAndKeeperDetailsModel],
                                    captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel)
                                   (implicit request: Request[AnyContent]) = {
    val viewModel = vehicleAndKeeperDetails match {
      case Some(details) => VehicleLookupFailureViewModel(details, vehicleAndKeeperLookupForm.replacementVRN)
      case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm)
    }

    val trackingId = request.cookies.trackingId()
    logMessage(trackingId, Info, s"Presenting payment failure view")
    Ok(views.html.vrm_assign.payment_failure(
      transactionId = transactionId,
      vehicleLookupFailureViewModel = viewModel,
      captureCertificateDetailsFormModel = captureCertificateDetailsFormModel,
      trackingId = trackingId)
    ).discardingCookies(removeCookiesOnExit)
  }
}