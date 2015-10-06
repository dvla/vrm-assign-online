package webserviceclients.vrmassignfulfil

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse

final case class VrmAssignFulfilResponse(documentNumber: Option[String])

final case class VrmAssignFulfilResponseDto(response: Option[MicroserviceResponse],
                                            vrmAssignFulfilResponse: VrmAssignFulfilResponse)

object VrmAssignFulfilResponse {

  implicit val JsonFormat = Json.format[VrmAssignFulfilResponse]
}

object VrmAssignFulfilResponseDto {

  implicit val JsonFormat = Json.format[VrmAssignFulfilResponseDto]
}