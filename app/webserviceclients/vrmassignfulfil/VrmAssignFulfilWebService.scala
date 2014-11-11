package webserviceclients.vrmassignfulfil

import play.api.libs.ws.WSResponse

import scala.concurrent.Future

trait VrmAssignFulfilWebService {

  def invoke(request: VrmAssignFulfilRequest, trackingId: String): Future[WSResponse]
}