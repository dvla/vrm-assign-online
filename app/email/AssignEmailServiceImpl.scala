package email

import java.io.FileWriter

import com.google.inject.Inject
import models.BusinessDetailsModel
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.Certificate.ExpiredWithFee
import models.ConfirmFormModel
import models.VehicleAndKeeperLookupFormModel
import org.apache.commons.codec.binary.Base64
import pdf.PdfService
import play.api.Play
import play.api.Play.current
import play.api.i18n.{Lang, Messages}
import play.twirl.api.HtmlFormat
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.Attachment
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailServiceSendRequest
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.From
import utils.helpers.Config
import views.html.vrm_assign.email_with_html
import views.html.vrm_assign.email_without_html

final class AssignEmailServiceImpl @Inject()(emailService: EmailService,
                                             pdfService: PdfService,
                                             config: Config) extends AssignEmailService with DVLALogger {

  private val from = From(email = config.emailSenderAddress, name = "DO NOT REPLY")
  private val govUkUrl = Some("public/images/gov-uk-email.jpg")

  def emailRequest(emailAddress: String,
                   vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                   captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                   captureCertificateDetailsModel: CaptureCertificateDetailsModel,
                   vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                   transactionTimestamp: String,
                   transactionId: String,
                   confirmFormModel: Option[ConfirmFormModel],
                   businessDetailsModel: Option[BusinessDetailsModel],
                   sendPdf: Boolean,
                   isKeeper: Boolean,
                   trackingId: TrackingId)(implicit lang: Lang): Option[EmailServiceSendRequest] = {

    val inputEmailAddressDomain = emailAddress.substring(emailAddress.indexOf("@"))

    if (config.emailWhitelist.isEmpty ||
      (config.emailWhitelist.get contains inputEmailAddressDomain.toLowerCase)) {

      val keeperName = Seq(vehicleAndKeeperDetailsModel.title,
        vehicleAndKeeperDetailsModel.firstName,
        vehicleAndKeeperDetailsModel.lastName
      ).flatten.mkString(" ")

      val plainTextMessage = populateEmailWithoutHtml(
        vehicleAndKeeperDetailsModel,
        captureCertificateDetailsFormModel,
        captureCertificateDetailsModel,
        vehicleAndKeeperLookupFormModel,
        transactionTimestamp,
        transactionId,
        confirmFormModel,
        businessDetailsModel,
        isKeeper
      )

      val message = htmlMessage(
        vehicleAndKeeperDetailsModel,
        captureCertificateDetailsFormModel,
        captureCertificateDetailsModel,
        vehicleAndKeeperLookupFormModel,
        transactionTimestamp,
        transactionId,
        confirmFormModel,
        businessDetailsModel,
        isKeeper
      ).toString()

      val subject = vehicleAndKeeperLookupFormModel.replacementVRN.replace(" ", "") +
        " " + Messages("email.email_service_impl.subject") +
        " " + vehicleAndKeeperDetailsModel.registrationNumber.replace(" ", "")

        val attachment: Option[Attachment] = if (sendPdf) {
          val pdf = pdfService.create(
            transactionId,
            keeperName,
            vehicleAndKeeperDetailsModel.address,
            vehicleAndKeeperLookupFormModel.replacementVRN.replace(" ", ""), trackingId
          )

          Some(new Attachment(
            Base64.encodeBase64URLSafeString(pdf),
            "application/pdf",
            "eV948.pdf",
            "Replacement registration number letter of authorisation"
          ))
        } else None

      Some(new EmailServiceSendRequest(
        plainTextMessage,
        message,
        attachment,
        from,
        subject,
        Option(List(emailAddress)),
        None
      ))
    } else {
      logMessage(trackingId, Error, s"Email not sent as email address $emailAddress is not in white list")
      None
    }
  }

  override def htmlMessage(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                           captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                           captureCertificateDetailsModel: CaptureCertificateDetailsModel,
                           vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                           transactionTimestamp: String,
                           transactionId: String,
                           confirmFormModel: Option[ConfirmFormModel],
                           businessDetailsModel: Option[BusinessDetailsModel],
                           isKeeper: Boolean)(implicit lang: Lang): HtmlFormat.Appendable = {

    val govUkContentId = govUkUrl match {
      case Some(filename) =>
        Play.resource(name = filename) match {
          case Some(resource) =>
            val imageInFile = resource.openStream()
            val imageData = org.apache.commons.io.IOUtils.toByteArray(imageInFile)
            "data:image/jpeg;base64," + Base64.encodeBase64String(imageData)
          case _ => ""
        }
      case _ => ""
    }

    email_with_html(
      vrm = vehicleAndKeeperDetailsModel.registrationNumber.trim,
      retentionCertId = captureCertificateDetailsFormModel.certificateDocumentCount + " " +
      captureCertificateDetailsFormModel.certificateDate + " " +
      captureCertificateDetailsFormModel.certificateTime + " " +
      captureCertificateDetailsFormModel.certificateRegistrationMark,
      transactionId = transactionId,
      transactionTimestamp = transactionTimestamp,
      keeperName = formatName(vehicleAndKeeperDetailsModel),
      keeperAddress = formatAddress(vehicleAndKeeperDetailsModel),
      replacementVRM = vehicleAndKeeperLookupFormModel.replacementVRN,
      outstandingFees = captureCertificateDetailsModel.certificate match { case ExpiredWithFee(_, _, fmtFee) => Some(fmtFee) case _ => None },
      govUkContentId = govUkContentId,
      keeperEmail = if (confirmFormModel.isDefined) confirmFormModel.get.keeperEmail else None,
      businessDetailsModel = businessDetailsModel,
      businessAddress = formatAddress(businessDetailsModel),
      isKeeper
    )
  }

  private def populateEmailWithoutHtml(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                                       captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                                       captureCertificateDetailsModel: CaptureCertificateDetailsModel,
                                       vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                                       transactionTimestamp: String,
                                       transactionId: String,
                                       confirmFormModel: Option[ConfirmFormModel],
                                       businessDetailsModel: Option[BusinessDetailsModel],
                                       isKeeper: Boolean)(implicit lang: Lang): String = {
    email_without_html(
      vrm = vehicleAndKeeperDetailsModel.registrationNumber.trim,
      retentionCertId = captureCertificateDetailsFormModel.certificateDocumentCount + " " +
      captureCertificateDetailsFormModel.certificateDate + " " +
      captureCertificateDetailsFormModel.certificateTime + " " +
      captureCertificateDetailsFormModel.certificateRegistrationMark,
      transactionId = transactionId,
      transactionTimestamp = transactionTimestamp,
      keeperName = formatName(vehicleAndKeeperDetailsModel),
      keeperAddress = formatAddress(vehicleAndKeeperDetailsModel),
      replacementVRM = vehicleAndKeeperLookupFormModel.replacementVRN,
      outstandingFees = captureCertificateDetailsModel.certificate match { case ExpiredWithFee(_, _, fmtFee) => Some(fmtFee) case _ => None },
      keeperEmail = if (confirmFormModel.isDefined) confirmFormModel.get.keeperEmail else None,
      businessDetailsModel = businessDetailsModel,
      businessAddress = formatAddress(businessDetailsModel),
      isKeeper
    ).toString()
  }

  private def formatName(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): String = {
    Seq(vehicleAndKeeperDetailsModel.title,
      vehicleAndKeeperDetailsModel.firstName,
      vehicleAndKeeperDetailsModel.lastName
    ).flatten.mkString(" ")
  }

  private def formatAddress(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): String = {
    vehicleAndKeeperDetailsModel.address match {
      case Some(adressModel) => adressModel.address.mkString(", ")
      case None => ""
    }
  }

  private def formatAddress(businessDetailsModel: Option[BusinessDetailsModel]): String = {
    businessDetailsModel match {
      case Some(details) => details.address.address.mkString(", ")
      case None => ""
    }
  }
}
