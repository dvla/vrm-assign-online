package webserviceclients.vrmassignfulfil

import play.api.libs.ws.WSResponse
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

trait VrmAssignFulfilWebService extends DVLALogger {

  def invoke(request: VrmAssignFulfilRequest, trackingId: TrackingId): Future[WSResponse]
}