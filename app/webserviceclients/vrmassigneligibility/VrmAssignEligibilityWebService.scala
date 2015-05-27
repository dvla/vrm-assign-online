package webserviceclients.vrmassigneligibility

import play.api.libs.ws.WSResponse
import scala.concurrent.Future

trait VrmAssignEligibilityWebService {

  def invoke(request: VrmAssignEligibilityRequest, trackingId: String): Future[WSResponse]
}