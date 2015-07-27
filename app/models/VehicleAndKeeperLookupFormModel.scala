package models

import mappings.common.vrm_assign.Postcode.postcode
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupFormModelBase
import uk.gov.dvla.vehicles.presentation.common.mappings.DocumentReferenceNumber.referenceNumber
import uk.gov.dvla.vehicles.presentation.common.mappings.VehicleRegistrationNumber.registrationNumber
import views.vrm_assign.KeeperConsent.keeperConsent
import views.vrm_assign.VehicleLookup.DocumentReferenceNumberId
import views.vrm_assign.VehicleLookup.KeeperConsentId
import views.vrm_assign.VehicleLookup.PostcodeId
import views.vrm_assign.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey
import views.vrm_assign.VehicleLookup.VehicleRegistrationNumberId
import views.vrm_assign.VehicleLookup.ReplacementVRN

/**
 * A form model for the vehicle lookup page
 * @param replacementVRN the new VRN to be assigned to the vehicle.
 * @param referenceNumber the current VRN number
 * @param registrationNumber the current VC5 registration number
 * @param postcode keeper's postcode
 * @param userType either the grantee or the business on behalf of the grantee
 */
final case class VehicleAndKeeperLookupFormModel(replacementVRN: String,
                                                 referenceNumber: String,
                                                 registrationNumber: String,
                                                 postcode: String,
                                                 userType: String) extends VehicleLookupFormModelBase {

  def isBusinessUserType = userType == views.vrm_assign.VehicleLookup.UserType_Business
  def isKeeperUserType = userType == views.vrm_assign.VehicleLookup.UserType_Keeper
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