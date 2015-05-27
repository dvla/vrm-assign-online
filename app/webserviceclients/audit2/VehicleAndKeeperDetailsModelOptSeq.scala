package webserviceclients.audit2

import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

object VehicleAndKeeperDetailsModelOptSeq {

  def from(vehicleAndKeeperDetailsModel: Option[VehicleAndKeeperDetailsModel]) = {
    vehicleAndKeeperDetailsModel match {
      case Some(model) =>
        val currentVrmOpt = Some(("currentVrm", model.registrationNumber))
        val makeOpt = model.make.map(make => ("make", make))
        val modelOpt = model.model.map(model => ("model", model))
        val keeperNameOpt = KeeperNameOptString.from(model).map(
          keeperName => ("keeperName", keeperName))
        val keeperAddressOpt = KeeperAddressOptString.from(model.address).map(
          keeperAddress => ("keeperAddress", keeperAddress))
        Seq(currentVrmOpt, makeOpt, modelOpt, keeperNameOpt, keeperAddressOpt)
      case _ => Seq.empty
    }
  }
}
