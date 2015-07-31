package webserviceclients.vrmassignfulfil

import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

import scala.concurrent.Future

trait VrmAssignFulfilWebService {

  def invoke(request: VrmAssignFulfilRequest, trackingId: TrackingId): Future[WSResponse]
}