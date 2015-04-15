package webserviceclients.audit2

import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

object VehicleAndKeeperDetailsModelOptSeq {

  def from(vehicleAndKeeperDetailsModel: Option[VehicleAndKeeperDetailsModel]) = {
    vehicleAndKeeperDetailsModel match {
      case Some(vehicleAndKeeperDetailsModel) => {
        val currentVrmOpt = Some(("currentVrm", vehicleAndKeeperDetailsModel.registrationNumber))
        val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
        val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
        val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
          keeperName => ("keeperName", keeperName))
        val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
          keeperAddress => ("keeperAddress", keeperAddress))
        Seq(currentVrmOpt, makeOpt, modelOpt, keeperNameOpt, keeperAddressOpt)
      }
      case _ => Seq.empty
    }
  }
}
