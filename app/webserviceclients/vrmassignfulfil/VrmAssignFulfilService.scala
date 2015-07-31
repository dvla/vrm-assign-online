package webserviceclients.vrmassignfulfil

import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

import scala.concurrent.Future

trait VrmAssignFulfilService {

  def invoke(cmd: VrmAssignFulfilRequest, trackingId: TrackingId): Future[VrmAssignFulfilResponse]
}