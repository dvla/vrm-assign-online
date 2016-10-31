package models

import models.Certificate.ExpiredWithFee

final case class DirectToPaperViewModel(vehicleLookupFailureViewModel: VehicleLookupFailureViewModel,
                                        certificate: Option[Certificate])

object DirectToPaperViewModel {

  def from(vehicleLookupFailureViewModel: VehicleLookupFailureViewModel,
            captureCertificateDetailsModel: Option[CaptureCertificateDetailsModel]): DirectToPaperViewModel =
    DirectToPaperViewModel(
      vehicleLookupFailureViewModel,
      captureCertificateDetailsModel.map(_.certificate)
    )
}
