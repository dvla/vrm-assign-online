package controllers

import com.google.inject.Inject
import models.BusinessDetailsModel
import models.CacheKeyPrefix
import models.CaptureCertificateDetailsModel
import models.CaptureCertificateDetailsFormModel
import models.ConfirmFormModel
import models.ConfirmViewModel
import models.FulfilModel
import models.VehicleAndKeeperLookupFormModel
import play.api.mvc.{Request, Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieKeyValue
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import utils.helpers.Config
import views.vrm_assign.Confirm.GranteeConsentCacheKey
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest

/**
 * This controller will present the outstanding payments.
 */
class ConfirmPayment @Inject()(auditService2: audit2.AuditService)
                              (implicit clientSideSessionFactory: ClientSideSessionFactory,
                               config: Config,
                               dateService: DateService) extends Controller {

  /**
   * In case we have a fulfil model present then, we landed on this page by mistake so redirect on error page. Otherwise,
   * proceed by checking that all the necessary models are present.
   * For ConfirmFormModel, we only require that it exists.
   * @return either the form or an error redirect
   */
  def present = Action { implicit request =>
    val redirectOnError = Redirect(routes.Confirm.present())

      request.cookies.getModel[FulfilModel].map( _ => redirectOnError) getOrElse {

      (for {
        vehicleAndKeeperLookupForm  <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
        vehicleDetails              <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
        captureCertDetails          <- request.cookies.getModel[CaptureCertificateDetailsModel]
        _                           <- request.cookies.getModel[ConfirmFormModel]
      } yield {
          val viewModel = ConfirmViewModel(vehicleDetails, vehicleAndKeeperLookupForm,
            captureCertDetails.outstandingDates, captureCertDetails.outstandingFees, vehicleAndKeeperLookupForm.userType)
          Ok(views.html.vrm_assign.confirm_payment(viewModel, vehicleDetails))

        }) getOrElse redirectOnError
    }

  }

  def exit = Action { implicit request =>
    Redirect(routes.LeaveFeedback.present()).discardingCookies(removeCookiesOnExit)
  }

  def submit = Action { implicit request =>

    (for {
      model <- request.cookies.getModel[ConfirmFormModel]
      granteeConsent = Some(CookieKeyValue(GranteeConsentCacheKey, model.granteeConsent))
      cookies = List(granteeConsent).flatten
    } yield {
        audit(AuditRequest.FeesDueToPay)
        Redirect(routes.Payment.begin()).withCookiesEx(cookies: _*).withCookie(model)
      }).getOrElse(Redirect(routes.Confirm.present()))

  }

  /**
   * Sends an audit message.
   * @param pageMovement the string that denotes the movement from page to page. This is one on the AuditRequest paths.
   * @param request implicit request to get the rest of the information needed.
   * @return unit.
   */
  def audit(pageMovement: String)(implicit request: Request[_]): Unit = {
    auditService2.send(AuditRequest.from(
      pageMovement = pageMovement,
      timestamp = dateService.dateTimeISOChronology,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
      captureCertificateDetailFormModel = request.cookies.getModel[CaptureCertificateDetailsFormModel],
      captureCertificateDetailsModel = request.cookies.getModel[CaptureCertificateDetailsModel],
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))
  }

}
