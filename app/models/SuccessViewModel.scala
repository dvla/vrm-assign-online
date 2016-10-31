package models

import models.Certificate.ExpiredWithFee
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final case class SuccessViewModel(vehicleDetails: VehicleAndKeeperDetailsModel,
                                  keeperEmail: Option[String],
                                  businessName: Option[String],
                                  businessContact: Option[String],
                                  businessEmail: Option[String],
                                  businessAddress: Option[AddressModel],
                                  prVrm: String,
                                  transactionId: String,
                                  transactionTimestamp: String,
                                  paymentMade: Boolean)

object SuccessViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            businessDetailsModelOpt: Option[BusinessDetailsModel],
            vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
            keeperEmail: Option[String],
            fulfilModel: FulfilModel,
            transactionId: String,
            certificate: Certificate): SuccessViewModel = {
    SuccessViewModel(
      vehicleDetails = vehicleAndKeeperDetails,
      keeperEmail = keeperEmail,
      businessName = for (businessDetails <- businessDetailsModelOpt) yield {
        businessDetails.name
      },
      businessContact = for (businessDetails <- businessDetailsModelOpt) yield {
        businessDetails.contact
      },
      businessEmail = for (businessDetails <- businessDetailsModelOpt) yield {
        businessDetails.email
      },
      businessAddress = for (businessDetails <- businessDetailsModelOpt) yield {
        businessDetails.address
      },
      prVrm = formatVrm(vehicleAndKeeperLookupFormModel.replacementVRN),
      transactionId,
      fulfilModel.transactionTimestamp,
      paymentMade = certificate.isInstanceOf[ExpiredWithFee]
    )
  }
}
