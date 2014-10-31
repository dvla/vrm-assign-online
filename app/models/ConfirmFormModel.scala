package models

import play.api.data.Forms.{mapping, optional}
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.email
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import views.vrm_assign.Confirm.{ConfirmCacheKey, KeeperEmailId}

final case class ConfirmFormModel(keeperEmail: Option[String])

object ConfirmFormModel {

  implicit val JsonFormat = Json.format[ConfirmFormModel]
  implicit val Key = CacheKey[ConfirmFormModel](ConfirmCacheKey)

  object Form {

    final val Mapping = mapping(
      KeeperEmailId -> optional(email)
    )(ConfirmFormModel.apply)(ConfirmFormModel.unapply)
  }

}