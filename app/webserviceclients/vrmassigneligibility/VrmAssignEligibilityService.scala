package webserviceclients.vrmassigneligibility

import scala.concurrent.Future

trait VrmAssignEligibilityService {

  def invoke(cmd: VrmAssignEligibilityRequest,
             trackingId: String): Future[VrmAssignEligibilityResponse]
}