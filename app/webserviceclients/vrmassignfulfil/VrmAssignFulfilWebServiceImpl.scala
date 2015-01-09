package webserviceclients.vrmassignfulfil

import com.google.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponse}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import utils.helpers.Config

import scala.concurrent.Future

final class VrmAssignFulfilWebServiceImpl @Inject()(config: Config) extends VrmAssignFulfilWebService {

  private val endPoint = s"${config.vrmAssignFulfilMicroServiceUrlBase}/vrm/assign/fulfil"

  override def invoke(request: VrmAssignFulfilRequest, trackingId: String): Future[WSResponse] = {
    val vrm = LogFormats.anonymize(request.currentVehicleRegistrationMark)

    Logger.debug(s"Calling vrm assign fulfil micro-service with request $vrm and tracking id: $trackingId")
    WS.url(endPoint).
      withHeaders(HttpHeaders.TrackingId -> trackingId).
      withRequestTimeout(config.vrmAssignFulfilRequestTimeout). // Timeout is in milliseconds
      post(Json.toJson(request))
  }
}