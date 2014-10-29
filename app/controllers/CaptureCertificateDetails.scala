package controllers

import com.google.inject.Inject
import models.{CaptureCertificateDetailsViewModel, CaptureCertificateDetailsFormModel, VehicleAndKeeperDetailsModel}
import play.api.data.{Form, FormError}
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import utils.helpers.Config
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.CaptureCertificateDetails._
import views.vrm_assign.VehicleLookup._
import scala.Some
import audit.{AuditMessage, AuditService}
import uk.gov.dvla.vehicles.presentation.common.services.DateService

final class CaptureCertificateDetails @Inject()(auditService: AuditService, dateService: DateService)
                                               (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                             config: Config) extends Controller {

  private[controllers] val form = Form(
    CaptureCertificateDetailsFormModel.Form.Mapping
  )

  def present = Action {
    implicit request =>
      request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
        case Some(vehicleAndKeeperDetails) =>
          val viewModel = CaptureCertificateDetailsViewModel(vehicleAndKeeperDetails)
          Ok(views.html.vrm_assign.capture_certificate_details(form.fill(), viewModel))
        case _ => Redirect(routes.VehicleLookup.present())
      }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => {
        request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
          case Some(vehicleAndKeeperDetails) =>
            val captureCertificateDetailsViewModel = CaptureCertificateDetailsViewModel(vehicleAndKeeperDetails)
            BadRequest(views.html.vrm_assign.capture_certificate_details(formWithReplacedErrors(invalidForm),
              captureCertificateDetailsViewModel))
          case _ =>
            Redirect(routes.VehicleLookup.present())
        }
      },
      validForm => Redirect(routes.LeaveFeedback.present()).withCookie(validForm) // TODO temp screen, replace with next screen in flow
    )
  }

  def exit = Action {
    implicit request =>
      auditService.send(AuditMessage.from(
        pageMovement = AuditMessage.CaptureCertificateDetailsToExit,
        transactionId = request.cookies.getString(TransactionIdCacheKey).get,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel]))
      Redirect(routes.LeaveFeedback.present()).discardingCookies(removeCookiesOnExit)
  }

  private def formWithReplacedErrors(form: Form[CaptureCertificateDetailsFormModel])(implicit request: Request[_]) =
    (form /: List(
      (ReferenceNumberId, "error.validReferenceNumber"),
      (PrVrmId, "error.validPrVrm"))) { (form, error) =>
      form.replaceError(error._1, FormError(
        key = error._1,
        message = error._2,
        args = Seq.empty
      ))
    }.distinctErrors
}
