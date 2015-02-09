package models

import play.api.data.Forms.{mapping, optional}
import play.api.libs.json.Json
import mappings.common.Consent.consent
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.email
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import views.vrm_assign.Confirm.{ConfirmCacheKey, GranteeConsentId, KeeperEmailId, SupplyEmailId, supplyEmail}

final case class ConfirmFormModel(keeperEmail: Option[String], granteeConsent: String, supplyEmail: String)

object ConfirmFormModel {

  implicit val JsonFormat = Json.format[ConfirmFormModel]
  implicit val Key = CacheKey[ConfirmFormModel](ConfirmCacheKey)

  object Form {

    final val Mapping = mapping(
      KeeperEmailId -> optional(email),
      GranteeConsentId -> consent,
      SupplyEmailId -> supplyEmail
    )(ConfirmFormModel.apply)(ConfirmFormModel.unapply).
      verifying("email-not-supplied", form => if (form.supplyEmail == "true") form.keeperEmail.isDefined else true) // When the user selects
    // that they want an email, they must provide an email.
  }

}