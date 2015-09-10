package webserviceclients.audit2

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel

object KeeperAddressOptString {

  def from(addressModel: Option[AddressModel]) = {
    addressModel match {
      case Some(address) =>
        if (address.address.nonEmpty) {
          var addressString = address.address.head
          for (addressLine <- address.address.drop(1)) {
            addressString += (", " + addressLine)
          }
          Some(addressString)
        } else {
          None
        }
      case _ =>
        None
    }
  }
}
