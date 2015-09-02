package models

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import views.vrm_assign.Fulfil.FulfilCacheKey

final case class FulfilModel(transactionTimestamp: String)

object FulfilModel {

  def from(transactionTimestamp: String) =
    FulfilModel(transactionTimestamp = transactionTimestamp)

  implicit val JsonFormat = Json.format[FulfilModel]
  implicit val Key = CacheKey[FulfilModel](FulfilCacheKey)
}