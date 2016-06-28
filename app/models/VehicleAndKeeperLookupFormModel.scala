package models

import mappings.common.vrm_assign.Postcode.postcode
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupFormModelBase
import uk.gov.dvla.vehicles.presentation.common.mappings.DocumentReferenceNumber.referenceNumber
import uk.gov.dvla.vehicles.presentation.common.mappings.VehicleRegistrationNumber.registrationNumber
import views.vrm_assign.KeeperConsent.keeperConsent
import views.vrm_assign.VehicleLookup.{DocumentReferenceNumberId, KeeperConsentId, PostcodeId, ReplacementVRN}
import views.vrm_assign.VehicleLookup.{UserType_Business, UserType_Keeper, VehicleAndKeeperLookupFormModelCacheKey, VehicleRegistrationNumberId}

final case class VehicleAndKeeperLookupFormModel(replacementVRN: String,
                                                 referenceNumber: String,
                                                 registrationNumber: String,
                                                 postcode: String,
                                                 userType: String) extends VehicleLookupFormModelBase {

  def isBusinessUserType = userType == UserType_Business
  def isKeeperUserType = userType == UserType_Keeper
}

object VehicleAndKeeperLookupFormModel {

  implicit val JsonFormat = Json.format[VehicleAndKeeperLookupFormModel]
  implicit val Key = CacheKey[VehicleAndKeeperLookupFormModel](VehicleAndKeeperLookupFormModelCacheKey)

  object Form {

    final val Mapping = mapping(
      ReplacementVRN -> registrationNumber,
      DocumentReferenceNumberId -> referenceNumber,
      VehicleRegistrationNumberId -> registrationNumber,
      PostcodeId -> postcode,
      KeeperConsentId -> keeperConsent
    )(VehicleAndKeeperLookupFormModel.apply)(VehicleAndKeeperLookupFormModel.unapply)
  }
}