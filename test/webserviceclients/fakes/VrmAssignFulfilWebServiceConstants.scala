package webserviceclients.fakes

import play.api.http.Status.OK
import webserviceclients.vrmassignfulfil.VrmAssignFulfilResponse

object VrmAssignFulfilWebServiceConstants {

  final val TransactionTimestampValid = "2014-10-11 11:51"

  def vrmAssignFulfilResponseSuccess: (Int, Option[VrmAssignFulfilResponse]) = {
    (OK, Some(VrmAssignFulfilResponse(documentNumber = Some("stub-document-number"), responseCode = None)))
  }
}