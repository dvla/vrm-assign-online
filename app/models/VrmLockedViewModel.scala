package models

final case class VrmLockedViewModel(vehicleLookupFailureViewModel: VehicleLookupFailureViewModel,
                                    timeString: String,
                                    javascriptTimestamp: Long)

object VrmLockedViewModel {

  def apply(vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
            timeString: String,
            javascriptTimestamp: Long): VrmLockedViewModel =
    VrmLockedViewModel(
      VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm),
      timeString,
      javascriptTimestamp
    )
}