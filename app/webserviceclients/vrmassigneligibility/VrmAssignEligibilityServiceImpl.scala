package webserviceclients.vrmassigneligibility

import javax.inject.Inject
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStats
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStatsFailure
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStatsSuccess

final class VrmAssignEligibilityServiceImpl @Inject()(ws: VrmAssignEligibilityWebService,
                                                      dateService: DateService,
                                                      healthStats: HealthStats)
  extends VrmAssignEligibilityService {

  override def invoke(cmd: VrmAssignEligibilityRequest,
                      trackingId: TrackingId)
                    : Future[(Int, VrmAssignEligibilityResponseDto)] = {
    import VrmAssignEligibilityServiceImpl.ServiceName
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) {
        healthStats.success(HealthStatsSuccess(ServiceName, dateService.now))
        (resp.status, resp.json.as[VrmAssignEligibilityResponseDto])
      }
      else if (resp.status == Status.INTERNAL_SERVER_ERROR) {
        val msg = s"Vrm Assign Eligibility micro-service call http status not OK, it was: ${resp.status}"
        val error = new RuntimeException(msg)
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, error))
        (resp.status, resp.json.as[VrmAssignEligibilityResponseDto])
      }
      else {
        val error = new RuntimeException(
          "Vrm Assign Eligibility micro-service call http status not OK, it " +
            s"was: ${resp.status}. Problem may come from either vrm-assign-eligibility micro-service or VSS"
        )
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, error))
        throw error
      }
    }.recover {
      case NonFatal(e) =>
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, e))
        throw e
    }
  }
}

object VrmAssignEligibilityServiceImpl {
  final val ServiceName = "vrm-assign-eligibility-microservice"
}