package webserviceclients.audit2

import models.BusinessDetailsModel

object BusinessAddressOptString {

  def from(businessDetailsModel: BusinessDetailsModel) = {
    var addressString = businessDetailsModel.name

    if (businessDetailsModel.address.address.nonEmpty) {
      for (addressLine <- businessDetailsModel.address.address) {
        addressString += (", " + addressLine)
      }
    }
    Some(addressString)
  }
}
