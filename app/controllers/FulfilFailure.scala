package controllers

import com.google.inject.Inject
import models.CacheKeyPrefix
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.PaymentModel
import models.VehicleAndKeeperLookupFormModel
import models.VehicleLookupFailureViewModel
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey
import webserviceclients.paymentsolve.PaymentSolveService

final class FulfilFailure @Inject()(paymentSolveService: PaymentSolveService)
                                   (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config,
                                    dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService
                                   ) extends Controller with DVLALogger {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[PaymentModel],
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[CaptureCertificateDetailsFormModel],
      request.cookies.getModel[CaptureCertificateDetailsModel]) match {

      case (Some(transactionId), paymentModelOpt, Some(vehicleAndKeeperLookupForm),
        captureCertificateDetailsFormModel, captureCertificateDetailsModel) =>
        logMessage(request.cookies.trackingId(), Info, s"Presenting fulfil failure view")
        Ok(
          views.html.vrm_assign.fulfil_failure(
            transactionId,
            paymentModelOpt.isDefined,
            VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm)
          )
        )
      case _ =>
        val msg = "user tried to go to FulfilFailure but the required cookie was not present"
        Redirect(routes.Error.present(msg))
    }
  }
}