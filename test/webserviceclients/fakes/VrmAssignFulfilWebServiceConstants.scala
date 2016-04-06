package webserviceclients.fakes

import play.api.http.Status.{FORBIDDEN, OK}
import webserviceclients.vrmassignfulfil.{VrmAssignFulfilResponse, VrmAssignFulfilResponseDto}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse

object VrmAssignFulfilWebServiceConstants {

  final val TransactionTimestampValid = "2014-10-11 11:51"
  final val FailureCodeUndefined = "blah"

  def vrmAssignFulfilResponseSuccess: (Int, VrmAssignFulfilResponseDto) = {
    (OK,
      VrmAssignFulfilResponseDto(
        None,
        VrmAssignFulfilResponse(Some("stub-document-number"))
      )
    )
  }

  def vrmAssignFulfilResponseFailure: (Int, VrmAssignFulfilResponseDto) = {
    (FORBIDDEN,
      VrmAssignFulfilResponseDto(
        Some(MicroserviceResponse("", "unknownErrorCode")),
        VrmAssignFulfilResponse(None)
      )
    )
  }
}
