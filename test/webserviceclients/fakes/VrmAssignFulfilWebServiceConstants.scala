package webserviceclients.fakes

import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import webserviceclients.vrmassignfulfil.{VrmAssignFulfilResponse, VrmAssignFulfilResponseDto}

object VrmAssignFulfilWebServiceConstants {

  final val TransactionTimestampValid = "2014-10-11 11:51"

  def vrmAssignFulfilResponseSuccess: (Int, VrmAssignFulfilResponseDto) = {
    (OK,
      VrmAssignFulfilResponseDto(
        None,
        VrmAssignFulfilResponse(Some("stub-document-number"))
      )
    )
  }
}
