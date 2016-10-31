package models

import models.Certificate.ExpiredWithFee
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final case class ConfirmViewModel(vehicleDetails: VehicleAndKeeperDetailsModel,
                                  replacementRegistration: String,
                                  outstandingPaymentAmount: Option[String],
                                  userType: String)

object ConfirmViewModel {
  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
            certificate: Certificate,
            userType: String): ConfirmViewModel =
    ConfirmViewModel(
      vehicleDetails = vehicleAndKeeperDetails,
      replacementRegistration = formatVrm(vehicleAndKeeperLookupFormModel.replacementVRN),
      outstandingPaymentAmount = certificate match { case ExpiredWithFee(_, _, fmtFee) => Some(fmtFee) case _ => None },
      userType = userType
    )
}
