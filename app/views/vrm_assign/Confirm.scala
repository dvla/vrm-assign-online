package views.vrm_assign

import models.CacheKeyPrefix
import play.api.data.Forms._
import play.api.data.Mapping

object Confirm {

  final val KeeperEmailMaxLength = 254
  final val SummaryId = "summary"
  final val ConfirmId = "confirm"
  final val ExitId = "exit"
  final val KeeperEmailId = "keeper-email"
  final val GranteeConsentId = "grantee-consent-id"
  final val ConfirmCacheKey = s"${CacheKeyPrefix}confirm"
  final val GranteeConsentCacheKey = s"${CacheKeyPrefix}grantee-consent"
  final val SupplyEmailId = "supply-email"
  final val SupplyEmail_true = "true"
  final val SupplyEmail_false = "false"

  def supplyEmail: Mapping[String] = nonEmptyText
}