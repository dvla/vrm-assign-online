package webserviceclients.vrmassigneligibility

import com.google.inject.Inject
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.api.libs.ws.WSResponse
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import utils.helpers.Config

final class VrmAssignEligibilityWebServiceImpl @Inject()(config: Config) extends VrmAssignEligibilityWebService {

  private val endPoint = s"${config.vrmAssignEligibilityMicroServiceUrlBase}/vrm/assign/eligibility"

  override def invoke(request: VrmAssignEligibilityRequest, trackingId: TrackingId): Future[WSResponse] = {
    val vrm = LogFormats.anonymize(request.currentVehicleRegistrationMark)

    logMessage(trackingId, Debug, s"Calling vrm assign eligibility micro-service with request $vrm")
    WS.url(endPoint).
      withHeaders(HttpHeaders.TrackingId -> trackingId.value).
      withRequestTimeout(config.vrmAssignEligibilityRequestTimeout). // Timeout is in milliseconds
      post(Json.toJson(request))
  }
}