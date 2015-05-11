package controllers

import com.google.inject.Inject
import models._
import play.api.data.Form
import play.api.data.FormError
import play.api.mvc.Result
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieKeyValue
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import utils.helpers.Config
import views.vrm_assign.Confirm.SupplyEmailId
import views.vrm_assign.Confirm.SupplyEmail_true
import views.vrm_assign.Confirm._
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup._
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest

final class Confirm @Inject()(
                               auditService2: audit2.AuditService
                               )
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config,
                              dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  private[controllers] val form = Form(ConfirmFormModel.Form.Mapping)

  def present = Action { implicit request =>
    (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[CaptureCertificateDetailsFormModel],
      request.cookies.getModel[CaptureCertificateDetailsModel],
      request.cookies.getModel[FulfilModel]) match {
      case (Some(vehicleAndKeeperLookupForm), Some(vehicleAndKeeper),
      Some(captureCertDetailsForm), Some(captureCertDetails), None) =>
        val viewModel = ConfirmViewModel(vehicleAndKeeper, vehicleAndKeeperLookupForm,
          captureCertDetails.outstandingDates, captureCertDetails.outstandingFees, vehicleAndKeeperLookupForm.userType)
        val emptyForm = form // Always fill the form with empty values to force user to enter new details. Also helps
      // with the situation where payment fails and they come back to this page via either back button or coming
      // forward from vehicle lookup - this could now be a different customer! We don't want the chance that one
      // customer gives up and then a new customer starts the journey in the same session and the email field is
      // pre-populated with the previous customer's address.
      val isKeeperEmailDisplayedOnLoad = false // Due to the form always being empty, the keeper email field will
      // always be hidden on first load
      val isKeeper = vehicleAndKeeperLookupForm.userType == UserType_Keeper
        Ok(views.html.vrm_assign.confirm(viewModel, emptyForm, isKeeperEmailDisplayedOnLoad, isKeeper))
      case _ =>
        Redirect(routes.CaptureCertificateDetails.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => handleInvalid(invalidForm),
      model => handleValid(model)
    )
  }

  private def formWithReplacedErrors(form: Form[ConfirmFormModel], id: String, msgId: String) =
    form.
      replaceError(
        KeeperEmailId,
        FormError(
          key = id,
          message = msgId,
          args = Seq.empty
        )
      ).
      replaceError(
        GranteeConsentId,
        "error.required",
        FormError(key = GranteeConsentId, message = "vrm_assign_confirm.grantee_consent.notgiven", args = Seq.empty)
      ).
      replaceError(
        key = "",
        message = "email-not-supplied",
        FormError(
          key = KeeperEmailId,
          message = "email-not-supplied"
        )
      )

  private def handleValid(model: ConfirmFormModel)(implicit request: Request[_]): Result = {
    val happyPath = request.cookies.getModel[VehicleAndKeeperLookupFormModel].map { vehicleAndKeeperLookup =>

      val granteeConsent = Some(CookieKeyValue(GranteeConsentCacheKey, model.granteeConsent))
      val cookies = List(granteeConsent).flatten

      val captureCertificateDetailsFormModel = request.cookies.getModel[CaptureCertificateDetailsFormModel].get
      val captureCertificateDetails = request.cookies.getModel[CaptureCertificateDetailsModel].get

      // check for outstanding fees
      if (captureCertificateDetails.outstandingFees > 0) {
        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.ConfirmToPayment,
          timestamp = dateService.dateTimeISOChronology,
          transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          keeperEmail = model.keeperEmail,
          captureCertificateDetailFormModel = Some(captureCertificateDetailsFormModel),
          captureCertificateDetailsModel = Some(captureCertificateDetails),
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))
        Redirect(routes.Payment.begin()).
          withCookiesEx(cookies: _*).
          withCookie(model)
      } else {
        //        auditService2.send(AuditRequest.from(
        //          pageMovement = AuditMessage.ConfirmToSuccess,
        //          timestamp = dateService.dateTimeISOChronology,
        //          transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
        //          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        //          keeperEmail = model.keeperEmail,
        //          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))
        Redirect(routes.Fulfil.fulfil()).
          withCookiesEx(cookies: _*).
          withCookie(model)
      }
    }
    val sadPath = Redirect(routes.Error.present("user went to Confirm handleValid without VehicleAndKeeperLookupFormModel cookie"))
    happyPath.getOrElse(sadPath)
  }

  private def handleInvalid(form: Form[ConfirmFormModel])(implicit request: Request[_]): Result = {
    val happyPath = for {
      vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      vehicleAndKeeper <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
      captureCertDetailsForm <- request.cookies.getModel[CaptureCertificateDetailsFormModel]
      captureCertDetails <- request.cookies.getModel[CaptureCertificateDetailsModel]}
      yield {
        val viewModel = ConfirmViewModel(vehicleAndKeeper, vehicleAndKeeperLookupForm,
          captureCertDetails.outstandingDates, captureCertDetails.outstandingFees,
          vehicleAndKeeperLookupForm.userType)
        val updatedForm = formWithReplacedErrors(form, KeeperEmailId, "error.validEmail").distinctErrors
        val isKeeperEmailDisplayedOnLoad = updatedForm.apply(SupplyEmailId).value == Some(SupplyEmail_true)
        val isKeeper = vehicleAndKeeperLookupForm.userType == UserType_Keeper
        BadRequest(views.html.vrm_assign.confirm(viewModel, updatedForm, isKeeperEmailDisplayedOnLoad, isKeeper))
      }
    val sadPath = Redirect(routes.Error.present("user went to Confirm handleInvalid without one of the required cookies"))
    happyPath.getOrElse(sadPath)
  }

  def exit = Action { implicit request =>
    auditService2.send(AuditRequest.from(
      pageMovement = AuditRequest.ConfirmToExit,
      timestamp = dateService.dateTimeISOChronology,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }
}