package webserviceclients.vrmretentioneligibility

import play.api.libs.json.Json

case class VrmAssignEligibilityResponse(esponseCode: Option[String])

object VrmAssignEligibilityResponse {

  implicit val JsonFormat = Json.format[VrmAssignEligibilityResponse]
}