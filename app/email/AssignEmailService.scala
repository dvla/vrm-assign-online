package email

import models._
import org.apache.commons.mail.HtmlEmail
import play.twirl.api.HtmlFormat

trait AssignEmailService {

  def sendEmail(emailAddress: String,
                vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                captureCertificateDetailsModel: CaptureCertificateDetailsModel,
                fulfilModel: FulfilModel,
                transactionId: String,
                confirmFormModel: Option[ConfirmFormModel],
                businessDetailsModel: Option[BusinessDetailsModel],
                isKeeper: Boolean)

  def htmlMessage(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                  captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                  captureCertificateDetailsModel: CaptureCertificateDetailsModel,
                  fulfilModel: FulfilModel,
                  transactionId: String,
                  confirmFormModel: Option[ConfirmFormModel],
                  businessDetailsModel: Option[BusinessDetailsModel],
                  isKeeper: Boolean): HtmlFormat.Appendable
}