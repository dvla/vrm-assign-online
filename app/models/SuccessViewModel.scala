package models

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final case class SuccessViewModel(registrationNumber: String,
                                  vehicleMake: Option[String],
                                  vehicleModel: Option[String],
                                  keeperTitle: Option[String],
                                  keeperFirstName: Option[String],
                                  keeperLastName: Option[String],
                                  keeperAddress: Option[AddressModel],
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
            outstandingPaymentList: List[String],
            outstandingPaymentAmount: Double): SuccessViewModel = {
    SuccessViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperDetails.registrationNumber),
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      keeperTitle = vehicleAndKeeperDetails.title,
      keeperFirstName = vehicleAndKeeperDetails.firstName,
      keeperLastName = vehicleAndKeeperDetails.lastName,
      keeperAddress = vehicleAndKeeperDetails.address,
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
      paymentMade = outstandingPaymentAmount > 0
    )
  }

  /** TODO check if this is used and if we can remove it */
  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
            vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
            fulfilModel: FulfilModel,
            transactionId: String,
            outstandingPaymentList: List[String],
            outstandingPaymentAmount: Double): SuccessViewModel = {
    SuccessViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperDetails.registrationNumber),
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      keeperTitle = vehicleAndKeeperDetails.title,
      keeperFirstName = vehicleAndKeeperDetails.firstName,
      keeperLastName = vehicleAndKeeperDetails.lastName,
      keeperAddress = vehicleAndKeeperDetails.address,
      keeperEmail = None,
      businessName = None,
      businessContact = None,
      businessEmail = None,
      businessAddress = None,
      prVrm = formatVrm(vehicleAndKeeperLookupFormModel.replacementVRN),
      transactionId,
      fulfilModel.transactionTimestamp,
      paymentMade = outstandingPaymentAmount > 0
    )
  }
}