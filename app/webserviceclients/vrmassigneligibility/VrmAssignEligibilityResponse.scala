package webserviceclients.vrmretentioneligibility

import play.api.libs.json._
import play.api.libs.json.Json.toJson
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

case class VrmAssignEligibilityResponse(lastDate: Option[DateTime], responseCode: Option[String])

object VrmAssignEligibilityResponse {

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

  implicit val JsonFormat = Json.format[VrmAssignEligibilityResponse]
}