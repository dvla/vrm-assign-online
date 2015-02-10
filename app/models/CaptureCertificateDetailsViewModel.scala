package models

import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final case class CaptureCertificateDetailsViewModel(registrationNumber: String,
                                               vehicleMake: Option[String],
                                               vehicleModel: Option[String])

object CaptureCertificateDetailsViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel): CaptureCertificateDetailsViewModel =
    CaptureCertificateDetailsViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperDetails.registrationNumber),
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model
    )
}