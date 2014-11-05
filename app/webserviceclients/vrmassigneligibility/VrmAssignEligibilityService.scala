package webserviceclients.vrmretentioneligibility

import scala.concurrent.Future

trait VrmAssignEligibilityService {

  def invoke(cmd: VrmAssignEligibilityRequest,
             trackingId: String): Future[VrmAssignEligibilityResponse]
}