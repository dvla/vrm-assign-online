package models

import play.api.data.Forms.mapping
import play.api.data.validation.Constraints
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.{AddressPicker, BusinessName}
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.emailConfirm
import uk.gov.dvla.vehicles.presentation.common.mappings.Postcode.postcode
import uk.gov.dvla.vehicles.presentation.common.model.Address
import views.vrm_assign.SetupBusinessDetails.BusinessContactId
import views.vrm_assign.SetupBusinessDetails.BusinessEmailId
import views.vrm_assign.SetupBusinessDetails.BusinessNameId
import views.vrm_assign.SetupBusinessDetails.BusinessPostcodeId
import views.vrm_assign.SetupBusinessDetails.SetupBusinessDetailsCacheKey

final case class SetupBusinessDetailsFormModel(name: String, contact: String, email: String, address: Address)

object SetupBusinessDetailsFormModel {

  implicit val addressFromat = Json.format[Address]
  implicit val JsonFormat = Json.format[SetupBusinessDetailsFormModel]
  implicit val Key = CacheKey[SetupBusinessDetailsFormModel](SetupBusinessDetailsCacheKey)

  object Form {

    final val Mapping = mapping(
      BusinessNameId -> BusinessName.businessNameMapping,
      BusinessContactId -> BusinessName.businessNameMapping,
      BusinessEmailId -> emailConfirm.verifying(Constraints.nonEmpty),
      BusinessPostcodeId -> AddressPicker.mapAddress
    )(SetupBusinessDetailsFormModel.apply)(SetupBusinessDetailsFormModel.unapply)
  }

}