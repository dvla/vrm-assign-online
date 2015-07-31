package webserviceclients.vrmassigneligibility

import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

import scala.concurrent.Future

trait VrmAssignEligibilityService {

  def invoke(cmd: VrmAssignEligibilityRequest,
             trackingId: TrackingId): Future[VrmAssignEligibilityResponse]
}