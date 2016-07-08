package models

import uk.gov.dvla.vehicles.presentation.common.mappings.Consent.consent
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.emailConfirm
import uk.gov.dvla.vehicles.presentation.common.mappings.OptionalToggle
import views.vrm_assign.Confirm.ConfirmCacheKey
import views.vrm_assign.Confirm.GranteeConsentId
import views.vrm_assign.Confirm.KeeperEmailId
import views.vrm_assign.Confirm.SupplyEmailId

final case class ConfirmFormModel(keeperEmail: Option[String], granteeConsent: String)

object ConfirmFormModel {

  implicit val JsonFormat = Json.format[ConfirmFormModel]
  implicit val Key = CacheKey[ConfirmFormModel](ConfirmCacheKey)

  object Form {

    final val Mapping = mapping(
      SupplyEmailId -> OptionalToggle.optional(emailConfirm.withPrefix(KeeperEmailId)),
      GranteeConsentId -> consent
    )(ConfirmFormModel.apply)(ConfirmFormModel.unapply)
  }
}