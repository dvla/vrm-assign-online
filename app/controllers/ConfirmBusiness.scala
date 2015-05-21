package controllers

import com.google.inject.Inject
import models.BusinessDetailsModel
import models.CacheKeyPrefix
import models.ConfirmBusinessViewModel
import models.FulfilModel
import models.SetupBusinessDetailsFormModel
import models.VehicleAndKeeperLookupFormModel
import play.api.mvc.{Action, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_assign.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest

final class ConfirmBusiness @Inject()(auditService2: audit2.AuditService)
                                     (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                      config: Config,
                                      dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService)
  extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[SetupBusinessDetailsFormModel],
      request.cookies.getModel[BusinessDetailsModel],
      request.cookies.getModel[FulfilModel]) match {
        case (Some(vehicleAndKeeperLookupForm), Some(vehicleAndKeeper),
          Some(setupBusinessDetailsFormModel), Some(businessDetailsModel), None) =>
          val viewModel = ConfirmBusinessViewModel(vehicleAndKeeper, Some(businessDetailsModel))
          Ok(views.html.vrm_assign.confirm_business(viewModel))
        case _ =>
          Redirect(routes.SetUpBusinessDetails.present())
      }
  }

  def submit = Action { implicit request =>
    handleValid()
  }

  def back = Action { implicit request =>
    Redirect(routes.SetUpBusinessDetails.present())
  }

  private def handleValid()(implicit request: Request[_]): Result = {
    val happyPath = request.cookies.getModel[VehicleAndKeeperLookupFormModel].map {
      vehicleAndKeeperLookup =>
        (request.cookies.getString(TransactionIdCacheKey),
          request.cookies.getModel[VehicleAndKeeperDetailsModel],
          request.cookies.getModel[BusinessDetailsModel],
          request.cookies.getModel[SetupBusinessDetailsFormModel]
          ) match {
          case (transactionId,
            vehicleAndKeeperDetailsModel,
            businessDetailsModel,
            setupBusinessDetailsFormModel) =>

            auditService2.send(AuditRequest.from(
              pageMovement = AuditRequest.ConfirmBusinessToCaptureCertificateDetails,
              transactionId = request.cookies.getString(TransactionIdCacheKey)
                .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
              timestamp = dateService.dateTimeISOChronology,
              vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
              businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))
            Redirect(routes.CaptureCertificateDetails.present())
              .withCookie(businessDetailsModel)
              .withCookie(setupBusinessDetailsFormModel)
        }
    }
    val msg = "user went to ConfirmBusiness handleValid without VehicleAndKeeperLookupFormModel cookie"
    val sadPath = Redirect(routes.Error.present(msg))
    happyPath.getOrElse(sadPath)
  }

  def exit = Action { implicit request =>
    auditService2.send(AuditRequest.from(
      pageMovement = AuditRequest.ConfirmBusinessToExit,
      transactionId = request.cookies.getString(TransactionIdCacheKey)
        .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }
}