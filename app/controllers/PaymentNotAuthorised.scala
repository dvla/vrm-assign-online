package controllers

import com.google.inject.Inject
import models.{CacheKeyPrefix, CaptureCertificateDetailsFormModel, VehicleLookupFailureViewModel, VehicleAndKeeperLookupFormModel}
import play.api.mvc.{Action, AnyContent, Controller, Request}
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey

final class PaymentNotAuthorised @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                             config: Config,
                                             dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService)
                                            extends Controller with DVLALogger {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[CaptureCertificateDetailsFormModel]) match {
      case (Some(transactionId), Some(vehicleAndKeeperLookupForm), Some(captureCertificateDetailsFormModel)) =>
        val vehicleAndKeeperDetails = request.cookies.getModel[VehicleAndKeeperDetailsModel]
        displayPaymentNotAuthorised(transactionId, vehicleAndKeeperLookupForm, vehicleAndKeeperDetails, captureCertificateDetailsFormModel)
      case _ =>
        logMessage( request.cookies.trackingId(), Warn, "PaymentNotAuthorised present cookie missing go to BeforeYouStart")
        Redirect(routes.BeforeYouStart.present())
    }
  }

  def submit = Action { implicit request =>
    request.cookies.getModel[VehicleAndKeeperLookupFormModel] match {
      case (Some(vehicleAndKeeperLookupFormModel)) =>
        Redirect(routes.VehicleLookup.present())
      case _ =>
        logMessage( request.cookies.trackingId(), Warn, "PaymentNotAuthorised submit cookie missing go to BeforeYouStart")
        Redirect(routes.BeforeYouStart.present())
    }
  }

  private def displayPaymentNotAuthorised(transactionId: String,
                                          vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
                                          vehicleAndKeeperDetails: Option[VehicleAndKeeperDetailsModel],
                                          captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel)
                                         (implicit request: Request[AnyContent]) = {
    val viewModel = vehicleAndKeeperDetails match {
      case Some(details) => VehicleLookupFailureViewModel(details)
      case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm)
    }

    Ok(views.html.vrm_assign.payment_not_authorised(
      transactionId = transactionId,
      vehicleLookupFailureViewModel = viewModel,
      data = vehicleAndKeeperLookupForm,
      captureCertificateDetailsFormModel = captureCertificateDetailsFormModel)
    )
  }
}