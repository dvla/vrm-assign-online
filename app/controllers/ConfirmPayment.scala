package controllers

import com.google.inject.Inject
import models.CacheKeyPrefix
import models.{ConfirmFormModel, CaptureCertificateDetailsModel, ConfirmViewModel, VehicleAndKeeperLookupFormModel}
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{CookieKeyValue, ClientSideSessionFactory}
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import utils.helpers.Config
import views.vrm_assign.Confirm._
import views.vrm_assign.RelatedCacheKeys._
import webserviceclients.audit2

/**
 * This controller will present the outstanding payments.
 */
class ConfirmPayment @Inject()(auditService2: audit2.AuditService)
                              (implicit clientSideSessionFactory: ClientSideSessionFactory,
                               config: Config,
                               dateService: DateService) extends Controller {

  def present = Action { implicit request =>
    (for {
      vehicleAndKeeperLookupForm  <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      vehicleDetails              <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
      captureCertDetails          <- request.cookies.getModel[CaptureCertificateDetailsModel]
    } yield {

      val viewModel = ConfirmViewModel(vehicleDetails, vehicleAndKeeperLookupForm,
        captureCertDetails.outstandingDates, captureCertDetails.outstandingFees, vehicleAndKeeperLookupForm.userType)
      Ok(views.html.vrm_assign.confirm_payment(viewModel, vehicleDetails))

    }).getOrElse(Redirect(routes.CaptureCertificateDetails.present()))

  }

  def exit = Action { implicit request =>
    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }

  def submit = Action { implicit request =>

    (for {
     model  <- request.cookies.getModel[ConfirmFormModel]
     granteeConsent = Some(CookieKeyValue(GranteeConsentCacheKey, model.granteeConsent))
     cookies = List(granteeConsent).flatten
    } yield {
      Redirect(routes.Payment.begin()).
        withCookiesEx(cookies: _*).
        withCookie(model)
    }).getOrElse(Redirect(routes.CaptureCertificateDetails.present()))

  }

}
