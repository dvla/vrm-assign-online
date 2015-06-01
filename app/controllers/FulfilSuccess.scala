package controllers

import com.google.inject.Inject
import email.AssignEmailService
import java.io.ByteArrayInputStream
import models.BusinessDetailsModel
import models.CacheKeyPrefix
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.ConfirmFormModel
import models.FulfilModel
import models.VehicleAndKeeperLookupFormModel
import pdf.PdfService
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.model.AddressModel
import common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_assign.VehicleLookup.{TransactionIdCacheKey, UserType_Business}
import webserviceclients.emailservice.EmailService
import webserviceclients.paymentsolve.PaymentSolveService

final class FulfilSuccess @Inject()(pdfService: PdfService,
                                    assignEmailService: AssignEmailService,
                                    paymentSolveService: PaymentSolveService,
                                    emailService: EmailService)
                                   (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config,
                                    dateService: common.services.DateService) extends Controller {

  def present = Action.async { implicit request =>
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
        val confirmFormModel = request.cookies.getModel[ConfirmFormModel]
        val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]

        val trackingId = request.cookies.trackingId()

        businessDetailsOpt.foreach { businessDetails =>
          assignEmailService.sendEmail(
            businessDetails.email,
            vehicleAndKeeperDetails,
            captureCertificateDetailsFormModel,
            captureCertificateDetailsModel,
            vehicleAndKeeperLookupForm,
            fulfilModel.transactionTimestamp,
            transactionId,
            confirmFormModel,
            businessDetailsModel,
            isKeeper = false, // US1589: Do not send keeper a pdf
            trackingId = trackingId
          )
        }

        keeperEmailOpt.foreach { keeperEmail =>
          assignEmailService.sendEmail(
            keeperEmail,
            vehicleAndKeeperDetails,
            captureCertificateDetailsFormModel,
            captureCertificateDetailsModel,
            vehicleAndKeeperLookupForm,
            fulfilModel.transactionTimestamp,
            transactionId,
            confirmFormModel,
            businessDetailsModel,
            isKeeper = true,
            trackingId = trackingId
          )
        }

        Future.successful(Redirect(routes.Success.present()))
      case _ =>
        Future.successful(Redirect(routes.Error.present("user tried to go to FulfilSuccess present without a required cookie")))
    }
  }

  def createPdf = Action {
    implicit request =>
      (request.cookies.getModel[VehicleAndKeeperLookupFormModel], request.cookies.getModel[CaptureCertificateDetailsFormModel],
        request.cookies.getString(TransactionIdCacheKey),
        request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
        case (Some(vehicleDetails), Some(captureCertificateDetailsFormModel), Some(transactionId), Some(vehicleAndKeeperDetails)) =>
          val keeperName = Seq(vehicleAndKeeperDetails.title, vehicleAndKeeperDetails.firstName, vehicleAndKeeperDetails.lastName).flatten.mkString(" ")

          val pdf = pdfService.create(
            transactionId,
            keeperName,
            vehicleAndKeeperDetails.address,
            vehicleDetails.replacementVRN.replace(" ", "")
          )
          val inputStream = new ByteArrayInputStream(pdf)
          val dataContent = Enumerator.fromStream(inputStream)
          // IMPORTANT: be very careful adding/changing any header information. You will need to run ALL tests after
          // and manually test after making any change.
          val newVRM = vehicleDetails.replacementVRN.replace(" ", "")
          val contentDisposition = "attachment;filename=" + newVRM + "-v948.pdf"
          Ok.feed(dataContent).withHeaders(
            CONTENT_TYPE -> "application/pdf",
            CONTENT_DISPOSITION -> contentDisposition
          )

        case _ => BadRequest("You are missing the cookies required to create a pdf")
      }
  }

  def next = Action { implicit request =>
    Redirect(routes.Success.present())
  }

  def emailStub = Action { implicit request =>
    Ok(assignEmailService.htmlMessage(
      vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel(
        registrationNumber = "stub-registrationNumber",
        make = Some("stub-make"),
        model = Some("stub-model"),
        title = Some("stub-title"),
        firstName = Some("stub-firstName"),
        lastName = Some("stub-lastName"),
        address = Some(AddressModel(address = Seq("stub-business-line1", "stub-business-line2",
          "stub-business-line3", "stub-business-line4", "stub-business-postcode"))),
        disposeFlag = None,
        keeperEndDate = None,
        keeperChangeDate = None,
        suppressedV5Flag = None
      ),
      captureCertificateDetailsFormModel = CaptureCertificateDetailsFormModel(
        certificateDocumentCount = "1",
        certificateDate = "11111",
        certificateTime = "111111",
        certificateRegistrationMark = "A1"),
      captureCertificateDetailsModel = CaptureCertificateDetailsModel("ABC123", None, List.empty, 0),
      vehicleAndKeeperLookupFormModel = VehicleAndKeeperLookupFormModel("GDS123", "A1", "11111111111", "QQ99QQ", ""),
      transactionTimestamp = "stub-transactionTimestamp",
      transactionId = "stub-transactionId",
      confirmFormModel = Some(ConfirmFormModel(
        keeperEmail = Some("stub-keeper-email"),
        granteeConsent = "true")
      ),
      businessDetailsModel = Some(BusinessDetailsModel(name = "stub-business-name",
        contact = "stub-business-contact",
        email = "stub-business-email",
        address = AddressModel(
          address = Seq("stub-business-line1", "stub-business-line2", "stub-business-line3",
            "stub-business-line4", "stub-business-postcode"))
      )
      ),
      isKeeper = true
    ))
  }

}