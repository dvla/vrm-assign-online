package webserviceclients.vrmassignfulfil

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.Json.toJson
import play.api.libs.json._

case class VrmAssignFulfilResponse(documentNumber: Option[String], responseCode: Option[String])

object VrmAssignFulfilResponse {
  implicit val JsonFormat = Json.format[VrmAssignFulfilResponse]
}