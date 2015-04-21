package controllers

import java.io.ByteArrayInputStream

import com.google.inject.Inject
import models._
import pdf.PdfService
import play.api.Logger
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup._
import webserviceclients.paymentsolve.PaymentSolveService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class Success @Inject()(
                               pdfService: PdfService,
                               paymentSolveService: PaymentSolveService
                               )
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config,
                              dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[CaptureCertificateDetailsFormModel],
      request.cookies.getModel[CaptureCertificateDetailsModel],
      request.cookies.getModel[FulfilModel]) match {

      case (Some(transactionId), Some(vehicleAndKeeperLookupForm), Some(vehicleAndKeeperDetails),
      Some(captureCertificateDetailsFormModel), Some(captureCertificateDetailsModel), Some(fulfilModel)) =>

        val businessDetailsOpt = request.cookies.getModel[BusinessDetailsModel].
          filter(_ => vehicleAndKeeperLookupForm.userType == UserType_Business)
        val keeperEmailOpt = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail)
        val successViewModel =
          SuccessViewModel(vehicleAndKeeperDetails, businessDetailsOpt, captureCertificateDetailsFormModel,
            keeperEmailOpt, fulfilModel, transactionId, captureCertificateDetailsModel.outstandingDates,
            captureCertificateDetailsModel.outstandingFees)

        Ok(views.html.vrm_assign.success(successViewModel, vehicleAndKeeperLookupForm.userType == UserType_Keeper))
      case _ =>
        Logger.warn("Success present user arrived without all of the required cookies")
        Redirect(routes.Confirm.present())
    }
  }

  def createPdf = Action.async { implicit request =>
    (request.cookies.getModel[CaptureCertificateDetailsFormModel],
      request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
      case (Some(captureCertificateDetailsFormModel), Some(transactionId), Some(vehicleAndKeeperDetails)) =>
        val keeperName = Seq(vehicleAndKeeperDetails.title, vehicleAndKeeperDetails.firstName, vehicleAndKeeperDetails.lastName).flatten.mkString(" ")
        pdfService.create(transactionId, keeperName, vehicleAndKeeperDetails.address,
          captureCertificateDetailsFormModel.prVrm.replace(" ", "")).map {
          pdf =>
            val inputStream = new ByteArrayInputStream(pdf)
            val dataContent = Enumerator.fromStream(inputStream)
            // IMPORTANT: be very careful adding/changing any header information. You will need to run ALL tests after
            // and manually test after making any change.
            val newVRM = captureCertificateDetailsFormModel.prVrm.replace(" ", "")
            val contentDisposition = "attachment;filename=" + newVRM + "-eV948.pdf"
            Ok.feed(dataContent).
              withHeaders(
                CONTENT_TYPE -> "application/pdf",
                CONTENT_DISPOSITION -> contentDisposition
              )
        }
      case _ => Future.successful {
        BadRequest("You are missing the cookies required to create a pdf")
      }
    }
  }

  def finish = Action { implicit request =>
    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }

  def successStub = Action { implicit request =>
    val successViewModel = SuccessViewModel(
      registrationNumber = "stub-registrationNumber",
      vehicleMake = Some("stub-vehicleMake"),
      vehicleModel = Some("stub-vehicleModel"),
      keeperTitle = Some("stub-keeperTitle"),
      keeperFirstName = Some("stub-keeperFirstName"),
      keeperLastName = Some("stub-keeperLastName"),
      keeperAddress = Some(AddressModel(address = Seq("stub-keeperAddress-line1", "stub-keeperAddress-line2"))),
      keeperEmail = Some("stub-keeperEmail"),
      businessName = Some("stub-businessName"),
      businessContact = Some("stub-"),
      businessEmail = Some("stub-businessContact"),
      businessAddress = Some(AddressModel(address = Seq("stub-businessAddress-line1", "stub-businessAddress-line2"))),
      prVrm = "A1",
      transactionId = "stub-transactionId",
      transactionTimestamp = "stub-transactionTimestamp",
      paymentMade = true
    )
    Ok(views.html.vrm_assign.success(successViewModel, true))
  }
}