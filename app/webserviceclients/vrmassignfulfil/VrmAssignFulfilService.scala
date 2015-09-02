package webserviceclients.vrmassignfulfil

import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

trait VrmAssignFulfilService {

  def invoke(cmd: VrmAssignFulfilRequest, trackingId: TrackingId): Future[VrmAssignFulfilResponse]
}