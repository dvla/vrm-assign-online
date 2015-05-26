package webserviceclients.audit2

import models.BusinessDetailsModel

object BusinessDetailsModelOptSeq {

  def from(businessDetailsModel: Option[BusinessDetailsModel]) = {
    businessDetailsModel match {
      case Some(model) =>
        val businessNameOpt = Some(("businessName", model.contact))
        val businessAddressOpt = BusinessAddressOptString.from(model).map(
          businessAddress => ("businessAddress", businessAddress))
        val businessEmailOpt = Some(("businessEmail", model.email))
        Seq(businessNameOpt, businessAddressOpt, businessEmailOpt)
      case _ => Seq.empty
    }
  }
}
