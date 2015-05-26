package webserviceclients.vrmassigneligibility

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{Json, JsValue, JsString, Writes}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebEndUserDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebHeaderDto

case class VrmAssignEligibilityRequest(webHeader: VssWebHeaderDto,
                                       currentVehicleRegistrationMark: String,
                                       certificateDocumentCount: String,
                                       certificateRegistrationMark: String,
                                       certificateDate: String,
                                       certificateTime: String,
                                       replacementVehicleRegistrationMark: String,
                                       v5DocumentReference: String,
                                       transactionTimestamp: DateTime)

object VrmAssignEligibilityRequest {

  // Handles this type of formatted string 2014-03-04T00:00:00.000Z
  implicit val jodaISODateWrites: Writes[DateTime] = new Writes[DateTime] {
    override def writes(dateTime: DateTime): JsValue = {
      val formatter = ISODateTimeFormat.dateTime
      JsString(formatter.print(dateTime))
    }
  }

  implicit val JsonFormatVssWebEndUserDto = Json.writes[VssWebEndUserDto]
  implicit val JsonFormatVssWebHeaderDto = Json.writes[VssWebHeaderDto]
  implicit val JsonFormat = Json.format[VrmAssignEligibilityRequest]
}