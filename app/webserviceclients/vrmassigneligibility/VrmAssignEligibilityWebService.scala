package webserviceclients.vrmassigneligibility

import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import scala.concurrent.Future

trait VrmAssignEligibilityWebService extends DVLALogger {

  def invoke(request: VrmAssignEligibilityRequest, trackingId: TrackingId): Future[WSResponse]
}