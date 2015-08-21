package webserviceclients.vrmassigneligibility

import play.api.libs.ws.WSResponse
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

trait VrmAssignEligibilityWebService extends DVLALogger {

  def invoke(request: VrmAssignEligibilityRequest, trackingId: TrackingId): Future[WSResponse]
}