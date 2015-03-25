package controllers

import java.io.ByteArrayInputStream

import com.google.inject.Inject
import email.AssignEmailService
import models._
import pdf.PdfService
import play.api.Logger
import play.api.libs.iteratee.Enumerator
import play.api.mvc.Result
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import views.vrm_assign.Confirm.SupplyEmail_true
import views.vrm_assign.Payment._
import views.vrm_assign.VehicleLookup._
import webserviceclients.paymentsolve.PaymentSolveService
import webserviceclients.paymentsolve.PaymentSolveUpdateRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class FulfilSuccess @Inject()(pdfService: PdfService,
                                    assignEmailService: AssignEmailService,
                                    paymentSolveService: PaymentSolveService)
                                   (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config,
                                    dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

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
        val keeperEmailOpt = request.cookies.getModel[ConfirmFormModel].flatMap { confirm =>
          if (confirm.supplyEmail == SupplyEmail_true) confirm.keeperEmail else None
        }
        val successViewModel =
          SuccessViewModel(vehicleAndKeeperDetails, businessDetailsOpt, captureCertificateDetailsFormModel,
            keeperEmailOpt, fulfilModel, transactionId, captureCertificateDetailsModel.outstandingDates,
            captureCertificateDetailsModel.outstandingFees)
        val confirmFormModel = request.cookies.getModel[ConfirmFormModel]
        val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]

        val trackingId = request.cookies.trackingId()

        businessDetailsOpt.foreach {
          businessDetails =>
            assignEmailService.sendEmail(
              businessDetails.email,
              vehicleAndKeeperDetails,
              captureCertificateDetailsFormModel,
              captureCertificateDetailsModel,
              fulfilModel,
              transactionId,
              confirmFormModel,
              businessDetailsModel,
              isKeeper = false, // US1589: Do not send keeper a pdf
              trackingId = trackingId
            )
        }

        keeperEmailOpt.foreach {
          keeperEmail =>
            assignEmailService.sendEmail(
              keeperEmail,
              vehicleAndKeeperDetails,
              captureCertificateDetailsFormModel,
              captureCertificateDetailsModel,
              fulfilModel,
              transactionId,
              confirmFormModel,
              businessDetailsModel,
              isKeeper = true,
              trackingId = trackingId
            )
        }

        request.cookies.getModel[PaymentModel] match {
          case Some(paymentModel) =>
            callUpdateWebPaymentService(
              paymentModel.trxRef.get,
              successViewModel,
              isKeeper = vehicleAndKeeperLookupForm.userType == UserType_Keeper,
              isPrimaryUrl = paymentModel.isPrimaryUrl
            )
          case _ => Future.successful(Redirect(routes.Success.present()))
        }
      case _ =>
        Future.successful(Redirect(routes.Error.present("user tried to go to FulfilSuccess present without a required cookie")))
    }
  }

  def createPdf = Action.async {
    implicit request =>
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
              val contentDisposition = "attachment;filename=" + newVRM + "-v948.pdf"
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
        certificateRegistrationMark = "A1",
        prVrm = "A1"),
      captureCertificateDetailsModel = CaptureCertificateDetailsModel("ABC123", None, List.empty, 0),
      fulfilModel = FulfilModel(transactionTimestamp = "stub-transactionTimestamp"),
      transactionId = "stub-transactionId",
      confirmFormModel = Some(ConfirmFormModel(keeperEmail = Some("stub-keeper-email"), granteeConsent = "true", supplyEmail = SupplyEmail_true)),
      businessDetailsModel = Some(BusinessDetailsModel(name = "stub-business-name", contact = "stub-business-contact", email = "stub-business-email", address = AddressModel(address = Seq("stub-business-line1", "stub-business-line2", "stub-business-line3", "stub-business-line4", "stub-business-postcode")))),
      isKeeper = true
    ))
  }

  private def callUpdateWebPaymentService(
                                           trxRef: String,
                                           successViewModel: SuccessViewModel,
                                           isKeeper: Boolean,
                                           isPrimaryUrl: Boolean
                                           )
                                         (implicit request: Request[_]): Future[Result] = {

    val transNo = request.cookies.getString(PaymentTransNoCacheKey).get

    val paymentSolveUpdateRequest = PaymentSolveUpdateRequest(
      transNo = transNo,
      trxRef = trxRef,
      authType = FulfilSuccess.SETTLE_AUTH_CODE,
      isPrimaryUrl = isPrimaryUrl
    )
    val trackingId = request.cookies.trackingId()
    paymentSolveService.invoke(paymentSolveUpdateRequest, trackingId).map { response =>
      Redirect(routes.Success.present())
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"SuccessPayment Payment Solve web service call with paymentSolveUpdateRequest failed. Exception " + e.toString)
        Redirect(routes.Success.present())
    }
  }
}

object FulfilSuccess {

  private val SETTLE_AUTH_CODE = "Settle"
}