package webserviceclients.vrmassignfulfil

import com.google.inject.Inject
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import utils.helpers.Config

final class VrmAssignFulfilWebServiceImpl @Inject()(config: Config) extends VrmAssignFulfilWebService {

  private val endPoint = s"${config.vrmAssignFulfilMicroServiceUrlBase}/vrm/assign/fulfil"

  override def invoke(request: VrmAssignFulfilRequest, trackingId: TrackingId): Future[WSResponse] = {
    val vrm = LogFormats.anonymize(request.currentVehicleRegistrationMark)

    logMessage(trackingId, Debug, s"Calling vrm assign fulfil micro-service with request $vrm ")
    WS.url(endPoint).
      withHeaders(HttpHeaders.TrackingId -> trackingId.value).
      withRequestTimeout(config.vrmAssignFulfilRequestTimeout). // Timeout is in milliseconds
      post(Json.toJson(request))
  }
}