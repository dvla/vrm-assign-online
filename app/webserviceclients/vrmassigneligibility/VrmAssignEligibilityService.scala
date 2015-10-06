package webserviceclients.vrmassigneligibility

import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

trait VrmAssignEligibilityService {

  def invoke(cmd: VrmAssignEligibilityRequest,
             trackingId: TrackingId)
            : Future[(Int, VrmAssignEligibilityResponseDto)]
}