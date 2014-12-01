package controllers

import audit._
import com.google.inject.Inject
import models._
import play.api.data.{Form, FormError}
import play.api.mvc.{Result, _}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClearTextClientSideSessionFactory, ClientSideSessionFactory, CookieKeyValue}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import utils.helpers.Config
import views.vrm_assign.Confirm._
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup._


final class Confirm @Inject()(auditService: AuditService, dateService: DateService)
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  private[controllers] val form = Form(ConfirmFormModel.Form.Mapping)

  def present = Action { implicit request =>
    val happyPath = for {
      vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      vehicleAndKeeper <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
      captureCertDetailsForm <- request.cookies.getModel[CaptureCertificateDetailsFormModel]
      captureCertDetails <- request.cookies.getModel[CaptureCertificateDetailsModel]
    } yield {
      val formModel = ConfirmFormModel(None)
      val viewModel = ConfirmViewModel(vehicleAndKeeper, captureCertDetailsForm,
        captureCertDetails.outstandingDates, captureCertDetails.outstandingFees)
      Ok(views.html.vrm_assign.confirm(viewModel, form.fill(formModel)))
    }
    val sadPath = Redirect(routes.VehicleLookup.present())
    happyPath.getOrElse(sadPath)
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => handleInvalid(invalidForm),
      model => handleValid(model)
    )
  }

  private def replaceErrorMsg(form: Form[ConfirmFormModel], id: String, msgId: String) =
    form.replaceError(
      KeeperEmailId,
      FormError(
        key = id,
        message = msgId,
        args = Seq.empty
      )
    )

  private def handleValid(model: ConfirmFormModel)(implicit request: Request[_]): Result = {
    val happyPath = request.cookies.getModel[VehicleAndKeeperLookupFormModel].map { vehicleAndKeeperLookup =>

      val keeperEmail = model.keeperEmail.map(CookieKeyValue(KeeperEmailCacheKey, _))

      val cookies = List(keeperEmail).flatten

      val captureCertificateDetails = request.cookies.getModel[CaptureCertificateDetailsModel].get

      // check for outstanding fees
      if (captureCertificateDetails.outstandingFees > 0) {
        auditService.send(AuditMessage.from(
          pageMovement = AuditMessage.ConfirmToPayment,
          timestamp = dateService.dateTimeISOChronology,
          transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          keeperEmail = model.keeperEmail,
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))
        Redirect(routes.Payment.begin()).withCookiesEx(cookies: _*)
      } else {
        auditService.send(AuditMessage.from(
          pageMovement = AuditMessage.ConfirmToSuccess,
          timestamp = dateService.dateTimeISOChronology,
          transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          keeperEmail = model.keeperEmail,
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))
        Redirect(routes.Fulfil.fulfil()).withCookiesEx(cookies: _*)
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
      captureCertDetails <- request.cookies.getModel[CaptureCertificateDetailsModel]    }
    yield {
      val viewModel = ConfirmViewModel(vehicleAndKeeper, captureCertDetailsForm,
        captureCertDetails.outstandingDates, captureCertDetails.outstandingFees)
      val updatedForm = replaceErrorMsg(form, KeeperEmailId, "error.validEmail").distinctErrors
      BadRequest(views.html.vrm_assign.confirm(viewModel, updatedForm))
    }
    val sadPath = Redirect(routes.Error.present("user went to Confirm handleInvalid without one of the required cookies"))
    happyPath.getOrElse(sadPath)
  }

  def exit = Action { implicit request =>
    auditService.send(AuditMessage.from(
      pageMovement = AuditMessage.ConfirmToExit,
      timestamp = dateService.dateTimeISOChronology,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      keeperEmail = request.cookies.getString(KeeperEmailCacheKey),
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }
}