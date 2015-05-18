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
import views.vrm_assign.Confirm._
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup._
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest

final class Confirm @Inject()(auditService2: audit2.AuditService)
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
      val isKeeper = vehicleAndKeeperLookupForm.userType == UserType_Keeper
        Ok(views.html.vrm_assign.confirm(viewModel, emptyForm, vehicleAndKeeper, isKeeper))
      case _ =>
        Redirect(routes.CaptureCertificateDetails.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(handleInvalid, handleValid)
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

    val sadPath = Redirect(routes.Error.present(
      "user went to Confirm handleValid without VehicleAndKeeperLookupFormModel cookie"))
    val granteeConsent = Some(CookieKeyValue(GranteeConsentCacheKey, model.granteeConsent))
    val cookies = List(granteeConsent).flatten

    (for {
      vehicleAndKeeperLookup <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      captureCertificateDetails <- request.cookies.getModel[CaptureCertificateDetailsModel]
    } yield {
        if (captureCertificateDetails.outstandingFees > 0) {
          audit(AuditRequest.ConfirmToFeesDue, Some(model))
          Redirect(routes.ConfirmPayment.present()).withCookiesEx(cookies: _*).withCookie(model)
        } else {
          audit(AuditRequest.ConfirmToSuccess, Some(model))
          Redirect(routes.Fulfil.fulfil()).withCookiesEx(cookies: _*).withCookie(model)
        }

      }) getOrElse sadPath
  }

  private def handleInvalid(form: Form[ConfirmFormModel])(implicit request: Request[_]): Result = (for {
    vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
    vehicleAndKeeper <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
    captureCertDetailsForm <- request.cookies.getModel[CaptureCertificateDetailsFormModel]
    captureCertDetails <- request.cookies.getModel[CaptureCertificateDetailsModel]
  } yield {
    val viewModel = ConfirmViewModel(vehicleAndKeeper, vehicleAndKeeperLookupForm,
      captureCertDetails.outstandingDates, captureCertDetails.outstandingFees,
      vehicleAndKeeperLookupForm.userType)
    val updatedForm = formWithReplacedErrors(form, KeeperEmailId, "error.validEmail").distinctErrors
    val isKeeper = vehicleAndKeeperLookupForm.userType == UserType_Keeper
    BadRequest(views.html.vrm_assign.confirm(viewModel, updatedForm, vehicleAndKeeper, isKeeper))
  }) getOrElse Redirect(routes.Error.present("user went to Confirm handleInvalid without one of the required cookies"))


  def exit = Action { implicit request =>
    audit(AuditRequest.ConfirmToExit, request.cookies.getModel[ConfirmFormModel])
    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }

  /**
   * Sends an audit message.
   * @param pageMovement the string that denotes the movement from page to page. This is one on the AuditRequest paths.
   * @param model, the confirm form model, from where we extract the keeper email if present.
   * @param request implicit request to get the rest of the information needed.
   * @return unit.
   */
  def audit(pageMovement: String, model: Option[ConfirmFormModel])(implicit request: Request[_]): Unit = {
    auditService2.send(AuditRequest.from(
      pageMovement = pageMovement,
      timestamp = dateService.dateTimeISOChronology,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      keeperEmail = model.flatMap(_.keeperEmail),
      captureCertificateDetailFormModel = request.cookies.getModel[CaptureCertificateDetailsFormModel],
      captureCertificateDetailsModel = request.cookies.getModel[CaptureCertificateDetailsModel],
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))
  }
}