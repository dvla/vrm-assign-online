package webserviceclients.vrmassigneligibility

import javax.inject.Inject
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

final class VrmAssignEligibilityServiceImpl @Inject()(ws: VrmAssignEligibilityWebService)
  extends VrmAssignEligibilityService {

  override def invoke(cmd: VrmAssignEligibilityRequest,
                      trackingId: TrackingId): Future[VrmAssignEligibilityResponse] =
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) resp.json.as[VrmAssignEligibilityResponse]
      else throw new RuntimeException(
        "Vrm Assign Eligibility web service call http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either vrm-assign-eligibility micro-service or the VSS"
      )
    }.recover {
      case NonFatal(e) => throw new RuntimeException("Vrm Assign Eligibility call failed for an unknown reason", e)
    }
}