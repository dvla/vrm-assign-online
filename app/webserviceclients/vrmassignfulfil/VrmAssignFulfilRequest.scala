package webserviceclients.vrmassignfulfil

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebEndUserDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebHeaderDto

case class VrmAssignFulfilRequest(webHeader: VssWebHeaderDto,
                                  currentVehicleRegistrationMark: String,
                                  certificateDocumentCount: String,
                                  certificateRegistrationMark: String,
                                  certificateDate: String,
                                  certificateTime: String,
                                  replacementVehicleRegistrationMark: String,
                                  v5DocumentReference: String,
                                  transactionTimestamp: DateTime)

object VrmAssignFulfilRequest {

  // Handles this type of formatted string 2014-03-04T00:00:00.000Z
  implicit val jodaISODateWrites: Writes[DateTime] = new Writes[DateTime] {
    override def writes(dateTime: DateTime): JsValue = {
      val formatter = ISODateTimeFormat.dateTime
      JsString(formatter.print(dateTime))
    }
  }
  implicit val JsonFormatVssWebEndUserDto = Json.writes[VssWebEndUserDto]
  implicit val JsonFormatVssWebHeaderDto = Json.writes[VssWebHeaderDto]
  implicit val JsonFormat = Json.format[VrmAssignFulfilRequest]
}