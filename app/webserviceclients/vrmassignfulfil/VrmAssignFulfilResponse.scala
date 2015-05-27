package webserviceclients.vrmassignfulfil

import play.api.libs.json.Json

case class VrmAssignFulfilResponse(documentNumber: Option[String], responseCode: Option[String])

object VrmAssignFulfilResponse {

  implicit val JsonFormat = Json.format[VrmAssignFulfilResponse]
}