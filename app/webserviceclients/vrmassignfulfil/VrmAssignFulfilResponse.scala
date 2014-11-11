package webserviceclients.vrmassignfulfil

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.Json.toJson
import play.api.libs.json._

case class VrmAssignFulfilResponse(lastDate: Option[DateTime], responseCode: Option[String])

object VrmAssignFulfilResponse {

  implicit object JsonDateFormatter extends Format[DateTime] {

    def writes(date: DateTime): JsValue = {
      toJson(ISODateTimeFormat.dateTime.print(date))
    }

    def reads(j: JsValue): JsResult[DateTime] = {
      try {
        val newDateTime = ISODateTimeFormat.dateTime().parseDateTime(j.as[String])
        JsSuccess(newDateTime)
      } catch {
        case e: Throwable => JsSuccess(new DateTime)
      }
    }

  }

  implicit val JsonFormat = Json.format[VrmAssignFulfilResponse]
}