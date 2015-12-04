package models

import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final case class ConfirmViewModel(vehicleDetails: VehicleAndKeeperDetailsModel,
                                  replacementRegistration: String,
                                  outstandingPaymentList: List[String],
                                  outstandingPaymentAmount: String,
                                  hasOutstandingPayments: Boolean,
                                  userType: String)

object ConfirmViewModel {
  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
            outstandingPaymentList: List[String],
            outstandingPaymentAmount: Int,
            userType: String): ConfirmViewModel =
    ConfirmViewModel(
      vehicleDetails = vehicleAndKeeperDetails,
      replacementRegistration = formatVrm(vehicleAndKeeperLookupFormModel.replacementVRN),
      outstandingPaymentList = outstandingPaymentList,
      outstandingPaymentAmount = f"${outstandingPaymentAmount / 100.0}%.2f",
      hasOutstandingPayments = outstandingPaymentAmount > 0,
      userType = userType
    )
}
