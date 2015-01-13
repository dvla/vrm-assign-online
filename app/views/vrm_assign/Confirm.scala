package views.vrm_assign

import models.CacheKeyPrefix

object Confirm {

  final val KeeperEmailMaxLength = 254
  final val ConfirmId = "confirm"
  final val ExitId = "exit"
  final val KeeperEmailId = "keeper-email"
  final val GranteeConsentId = "grantee-consent-id"
  final val ConfirmCacheKey = s"${CacheKeyPrefix}confirm"
  final val KeeperEmailCacheKey = s"${CacheKeyPrefix}keeper-email"
  final val GranteeConsentCacheKey = s"${CacheKeyPrefix}grantee-consent"
}