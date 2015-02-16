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
                                  outstandingPaymentList: List[String],
                                  outstandingPaymentAmount: Double)

object SuccessViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            businessDetailsModelOpt: Option[BusinessDetailsModel],
            captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
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
      prVrm = formatVrm(captureCertificateDetailsFormModel.prVrm),
      transactionId,
      fulfilModel.transactionTimestamp,
      outstandingPaymentList = outstandingPaymentList,
      outstandingPaymentAmount = outstandingPaymentAmount
    )
  }

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
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
      prVrm = formatVrm(captureCertificateDetailsFormModel.prVrm),
      transactionId,
      fulfilModel.transactionTimestamp,
      outstandingPaymentList = outstandingPaymentList,
      outstandingPaymentAmount = outstandingPaymentAmount
    )
  }
}