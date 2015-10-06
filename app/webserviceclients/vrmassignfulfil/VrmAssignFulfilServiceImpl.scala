package webserviceclients.vrmassignfulfil

import javax.inject.Inject
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

final class VrmAssignFulfilServiceImpl @Inject()(ws: VrmAssignFulfilWebService) extends VrmAssignFulfilService {

  override def invoke(cmd: VrmAssignFulfilRequest, trackingId: TrackingId):
                                                                          Future[(Int, VrmAssignFulfilResponseDto)] = {
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK)
        (resp.status, resp.json.as[VrmAssignFulfilResponseDto])
      else if (resp.status == Status.INTERNAL_SERVER_ERROR)
        (resp.status, resp.json.as[VrmAssignFulfilResponseDto])
      else throw new RuntimeException(
        "Vrm Assign Fulfil web service call http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either vrm-assign-fulfil micro-service or the VSS"
      )
    }
  }
}