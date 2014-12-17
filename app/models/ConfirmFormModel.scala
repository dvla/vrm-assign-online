package models

import play.api.data.Forms.{mapping, optional}
import play.api.libs.json.Json
import mappings.common.Consent.consent
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.email
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import views.vrm_assign.Confirm.{ConfirmCacheKey, GranteeConsentId, KeeperEmailId}

final case class ConfirmFormModel(keeperEmail: Option[String], granteeConsent: String)

object ConfirmFormModel {

  implicit val JsonFormat = Json.format[ConfirmFormModel]
  implicit val Key = CacheKey[ConfirmFormModel](ConfirmCacheKey)

  object Form {

    final val Mapping = mapping(
      KeeperEmailId -> optional(email),
      GranteeConsentId -> consent
    )(ConfirmFormModel.apply)(ConfirmFormModel.unapply)
  }

}