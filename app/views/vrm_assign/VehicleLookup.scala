package views.vrm_assign

import models.CacheKeyPrefix

object VehicleLookup {

  final val ReplacementVRN = "document-replacement-vrn"
  final val DocumentReferenceNumberId = "document-reference-number"
  final val VehicleRegistrationNumberId = "vehicle-registration-number"
  final val PostcodeId = "postcode"
  final val KeeperConsentId = "keeper-consent"
  final val VehicleAndKeeperLookupFormModelCacheKey = s"${CacheKeyPrefix}vehicle-and-keeper-lookup-form-model"
  final val TransactionIdCacheKey = s"${CacheKeyPrefix}transaction-Id"
  final val SubmitId = "submit"
  final val ExitId = "exit"
  final val UserType_Keeper = "Keeper"
  final val UserType_Business = "Business"
}