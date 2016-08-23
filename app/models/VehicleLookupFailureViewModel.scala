package models

import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final case class VehicleLookupFailureViewModel(registrationNumber: String,
                                               make: Option[String],
                                               model: Option[String],
                                               replacementVRN: String,
                                               v5ref: String,
                                               postcode: String,
                                               failureCode: String)

object VehicleLookupFailureViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
            failureCode: String)(implicit config: utils.helpers.Config): VehicleLookupFailureViewModel =
    VehicleLookupFailureViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperDetails.registrationNumber),
      make = vehicleAndKeeperDetails.make,
      model = vehicleAndKeeperDetails.model,
      replacementVRN = formatVrm(vehicleAndKeeperLookupForm.replacementVRN),
      v5ref = vehicleAndKeeperLookupForm.referenceNumber,
      postcode = vehicleAndKeeperLookupForm.postcode,
      failureCode = filteredFailureCode(failureCode)
  )

  def apply(vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
            failureCode: String)(implicit config: utils.helpers.Config): VehicleLookupFailureViewModel =
    VehicleLookupFailureViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperLookupForm.registrationNumber),
      make = None,
      model = None,
      replacementVRN = formatVrm(vehicleAndKeeperLookupForm.replacementVRN),
      v5ref = vehicleAndKeeperLookupForm.referenceNumber,
      postcode = vehicleAndKeeperLookupForm.postcode,
      failureCode = filteredFailureCode(failureCode)
    )

  private def filteredFailureCode(code: String)(implicit config: utils.helpers.Config): String =
    config.failureCodeBlacklist match {
      case Some(failureCodes) =>
        if (failureCodes.contains(code)) ""
        else code
      case _ => code
    }
}
