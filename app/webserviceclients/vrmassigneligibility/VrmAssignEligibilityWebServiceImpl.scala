package webserviceclients.vrmretentioneligibility

import com.google.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import utils.helpers.Config

import scala.concurrent.Future

final class VrmAssignEligibilityWebServiceImpl @Inject()(config: Config) extends VrmAssignEligibilityWebService {

  private val endPoint = s"${config.vrmAssignEligibilityMicroServiceUrlBase}/vrm/assign/eligibility"

  override def invoke(request: VrmAssignEligibilityRequest, trackingId: String): Future[WSResponse] = {
    val vrm = LogFormats.anonymize(request.currentVehicleRegistrationMark)

    Logger.debug(s"Calling vrm assign eligibility micro-service with request $vrm and tracking id: $trackingId")
    WS.url(endPoint).
      withHeaders(HttpHeaders.TrackingId -> trackingId).
      withRequestTimeout(config.vrmAssignEligibilityRequestTimeout). // Timeout is in milliseconds
      post(Json.toJson(request))
  }
}