package models

import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final case class VehicleLookupFailureViewModel(registrationNumber: String,
                                               make: Option[String],
                                               model: Option[String],
                                               replacementVRN: String)

object VehicleLookupFailureViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            replacementVRN: String): VehicleLookupFailureViewModel =
    VehicleLookupFailureViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperDetails.registrationNumber),
      make = vehicleAndKeeperDetails.make,
      model = vehicleAndKeeperDetails.model,
      replacementVRN = formatVrm(replacementVRN)
  )

  def apply(vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel): VehicleLookupFailureViewModel =
    VehicleLookupFailureViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperLookupForm.registrationNumber),
      make = None,
      model = None,
      replacementVRN = formatVrm(vehicleAndKeeperLookupForm.replacementVRN)
    )
}