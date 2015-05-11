package email

import models._
import play.twirl.api.HtmlFormat
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

trait AssignEmailService {

  def sendEmail(emailAddress: String,
                vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                captureCertificateDetailsModel: CaptureCertificateDetailsModel,
                vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                fulfilModel: FulfilModel,
                transactionId: String,
                confirmFormModel: Option[ConfirmFormModel],
                businessDetailsModel: Option[BusinessDetailsModel],
                isKeeper: Boolean,
                trackingId: String)

  def htmlMessage(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                  captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                  captureCertificateDetailsModel: CaptureCertificateDetailsModel,
                  vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                  fulfilModel: FulfilModel,
                  transactionId: String,
                  confirmFormModel: Option[ConfirmFormModel],
                  businessDetailsModel: Option[BusinessDetailsModel],
                  isKeeper: Boolean): HtmlFormat.Appendable
}