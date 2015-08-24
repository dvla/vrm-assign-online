package webserviceclients.vrmassignfulfil

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{Json, JsString, JsValue, Writes}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebEndUserDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebHeaderDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.Attachment
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailServiceSendRequest
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.From
import webserviceclients.paymentsolve.PaymentSolveUpdateRequest

case class VrmAssignFulfilRequest(webHeader: VssWebHeaderDto,
                                  currentVehicleRegistrationMark: String,
                                  certificateDocumentCount: String,
                                  certificateRegistrationMark: String,
                                  certificateDate: String,
                                  certificateTime: String,
                                  replacementVehicleRegistrationMark: String,
                                  v5DocumentReference: String,
                                  transactionTimestamp: DateTime,
                                  paymentSolveUpdateRequest: Option[PaymentSolveUpdateRequest],
                                  successEmailRequests:Seq[EmailServiceSendRequest],
                                  failureEmailRequests:Seq[EmailServiceSendRequest])

object VrmAssignFulfilRequest {

  // Handles this type of formatted string 2014-03-04T00:00:00.000Z
  implicit val jodaISODateWrites: Writes[DateTime] = new Writes[DateTime] {
    override def writes(dateTime: DateTime): JsValue = {
      val formatter = ISODateTimeFormat.dateTime
      JsString(formatter.print(dateTime))
    }
  }

  implicit val JsonFormatFrom = Json.format[From]
  implicit val JsonFormatAttachment = Json.format[Attachment]
  implicit val JsonFormatEmailServiceSendRequest = Json.format[EmailServiceSendRequest]
  implicit val JsonFormatPaymentSolveUpdateRequest = Json.format[PaymentSolveUpdateRequest]
  implicit val JsonFormatVssWebEndUserDto = Json.writes[VssWebEndUserDto]
  implicit val JsonFormatVssWebHeaderDto = Json.writes[VssWebHeaderDto]
  implicit val JsonFormat = Json.format[VrmAssignFulfilRequest]
}