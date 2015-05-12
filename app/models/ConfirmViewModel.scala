package models

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final case class ConfirmViewModel(registrationNumber: String,
                                  replacementRegistration: String,
                                  vehicleMake: Option[String],
                                  vehicleModel: Option[String],
                                  keeperTitle: Option[String],
                                  keeperFirstName: Option[String],
                                  keeperLastName: Option[String],
                                  keeperAddress: Option[AddressModel],
                                  outstandingPaymentList: List[String],
                                  outstandingPaymentAmount: Double,
                                  userType: String)

object ConfirmViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
            outstandingPaymentList: List[String],
            outstandingPaymentAmount: Double,
            userType: String): ConfirmViewModel =
    ConfirmViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperDetails.registrationNumber),
      replacementRegistration = formatVrm(vehicleAndKeeperLookupFormModel.replacementVRN),
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      keeperTitle = vehicleAndKeeperDetails.title,
      keeperFirstName = vehicleAndKeeperDetails.firstName,
      keeperLastName = vehicleAndKeeperDetails.lastName,
      keeperAddress = vehicleAndKeeperDetails.address,
      outstandingPaymentList = outstandingPaymentList,
      outstandingPaymentAmount = outstandingPaymentAmount,
      userType = userType
    )
}
