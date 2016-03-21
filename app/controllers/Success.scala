package controllers

import com.google.inject.Inject
import java.io.ByteArrayInputStream
import models.BusinessDetailsModel
import models.CacheKeyPrefix
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.ConfirmFormModel
import models.FulfilModel
import models.SuccessViewModel
import models.VehicleAndKeeperLookupFormModel
import pdf.PdfService
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup.{TransactionIdCacheKey, UserType_Business, UserType_Keeper}

final class Success @Inject()(pdfService: PdfService)
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config,
                              dateService: DateService) extends Controller with DVLALogger {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[CaptureCertificateDetailsFormModel],
      request.cookies.getModel[CaptureCertificateDetailsModel],
      request.cookies.getModel[FulfilModel]) match {

      case (Some(transactionId),
            Some(vehicleAndKeeperLookupForm),
            Some(vehicleAndKeeperDetails),
            Some(captureCertificateDetailsFormModel),
            Some(captureCertificateDetailsModel),
            Some(fulfilModel)) =>

        val businessDetailsOpt = request.cookies.getModel[BusinessDetailsModel].
          filter(_ => vehicleAndKeeperLookupForm.userType == UserType_Business)
        val keeperEmailOpt = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail)
        val successViewModel = SuccessViewModel(
          vehicleAndKeeperDetails,
          businessDetailsOpt,
          vehicleAndKeeperLookupForm,
          keeperEmailOpt,
          fulfilModel,
          transactionId,
          captureCertificateDetailsModel.outstandingDates,
          captureCertificateDetailsModel.outstandingFees
        )
        logMessage(request.cookies.trackingId(), Info,
          "User transaction completed successfully - now displaying the assign success view"
        )
        Ok(views.html.vrm_assign.success(successViewModel, vehicleAndKeeperLookupForm.userType == UserType_Keeper))
      case _ =>
        val msg = "User transaction completed successfully but not displaying the success view " +
          "because the user arrived without all of the required cookies"
        logMessage(request.cookies.trackingId(), Warn, msg)
        Redirect(routes.Confirm.present())
    }
  }

  def createPdf = Action { implicit request =>
    ( request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
      case (Some(vehicleAndKeeperLookupFormModel), Some(transactionId), Some(vehicleAndKeeperDetails)) =>
        val pdf =  pdfService.create(
          transactionId,
          Seq(
            vehicleAndKeeperDetails.title,
            vehicleAndKeeperDetails.firstName,
            vehicleAndKeeperDetails.lastName
          ).flatten.mkString(" "),
          vehicleAndKeeperDetails.address,
          vehicleAndKeeperLookupFormModel.replacementVRN.replace(" ", ""), request.cookies.trackingId()
        )
        val inputStream = new ByteArrayInputStream(pdf)
        val dataContent = Enumerator.fromStream(inputStream)
        // IMPORTANT: be very careful adding/changing any header information. You will need to run ALL tests after
        // and manually test after making any change.
        val newVRM =  vehicleAndKeeperLookupFormModel.replacementVRN.replace(" ", "")
        val contentDisposition = "attachment;filename=" + newVRM + "-eV948.pdf"
        Ok.feed(dataContent).withHeaders(
          CONTENT_TYPE -> "application/pdf",
          CONTENT_DISPOSITION -> contentDisposition
        )
      case _ => BadRequest("You are missing the cookies required to create a pdf")
    }
  }

  def finish = Action { implicit request =>
    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }
}
