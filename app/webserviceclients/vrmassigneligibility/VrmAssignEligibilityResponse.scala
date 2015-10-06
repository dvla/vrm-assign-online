package webserviceclients.vrmassigneligibility

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.Json.toJson
import play.api.libs.json.{Format, Json, JsResult, JsSuccess, JsValue}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse

final case class VrmAssignEligibilityResponse(certificateExpiryDate: Option[DateTime])

final case class VrmAssignEligibilityResponseDto(response: Option[MicroserviceResponse],
                                                 vrmAssignEligibilityResponse: VrmAssignEligibilityResponse)


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

object VrmAssignEligibilityResponseDto {
  implicit val JsonFormat = Json.format[VrmAssignEligibilityResponseDto]
}
