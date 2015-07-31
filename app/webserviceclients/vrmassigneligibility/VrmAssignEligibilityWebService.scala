package webserviceclients.vrmassigneligibility

import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import scala.concurrent.Future

trait VrmAssignEligibilityWebService {

  def invoke(request: VrmAssignEligibilityRequest, trackingId: TrackingId): Future[WSResponse]
}