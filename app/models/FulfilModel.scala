package models

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import views.vrm_assign.Fulfil._

final case class FulfilModel(certificateNumber: String, transactionTimestamp: String)

object FulfilModel {

  def from(certificateNumber: String, transactionTimestamp: String) =
    FulfilModel(certificateNumber = certificateNumber,
      transactionTimestamp = transactionTimestamp)

  implicit val JsonFormat = Json.format[FulfilModel]
  implicit val Key = CacheKey[FulfilModel](FulfilCacheKey)
}