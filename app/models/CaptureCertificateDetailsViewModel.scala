package models

final case class CaptureCertificateDetailsViewModel(registrationNumber: String,
                                               vehicleMake: Option[String],
                                               vehicleModel: Option[String])

object CaptureCertificateDetailsViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel): CaptureCertificateDetailsViewModel =
    CaptureCertificateDetailsViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model
    )
}