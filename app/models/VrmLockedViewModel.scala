package models

import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final case class VrmLockedViewModel(vehicleLookupFailureViewModel: VehicleLookupFailureViewModel,
                                    timeString: String,
                                    javascriptTimestamp: Long)

object VrmLockedViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            timeString: String,
            javascriptTimestamp: Long): VrmLockedViewModel =
    VrmLockedViewModel(
      VehicleLookupFailureViewModel(vehicleAndKeeperDetails),
      timeString,
      javascriptTimestamp
    )

  def apply(vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
            timeString: String,
            javascriptTimestamp: Long): VrmLockedViewModel =
    VrmLockedViewModel(
      VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm),
      timeString,
      javascriptTimestamp
    )
}