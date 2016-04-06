package email

import models.BusinessDetailsModel
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.ConfirmFormModel
import models.VehicleAndKeeperLookupFormModel
import play.twirl.api.HtmlFormat
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailServiceSendRequest

trait AssignEmailService {

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
                   trackingId: TrackingId): Option[EmailServiceSendRequest]

  def htmlMessage(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                  captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                  captureCertificateDetailsModel: CaptureCertificateDetailsModel,
                  vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                  transactionTimestamp: String,
                  transactionId: String,
                  confirmFormModel: Option[ConfirmFormModel],
                  businessDetailsModel: Option[BusinessDetailsModel],
                  isKeeper: Boolean): HtmlFormat.Appendable
}
