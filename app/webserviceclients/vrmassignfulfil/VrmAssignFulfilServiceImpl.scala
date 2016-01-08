package webserviceclients.vrmassignfulfil

import javax.inject.Inject
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStats
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStatsFailure
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStatsSuccess

import scala.util.control.NonFatal

final class VrmAssignFulfilServiceImpl @Inject()(ws: VrmAssignFulfilWebService,
                                                 dateService: DateService,
                                                 healthStats: HealthStats) extends VrmAssignFulfilService {

  override def invoke(cmd: VrmAssignFulfilRequest, trackingId: TrackingId): Future[(Int, VrmAssignFulfilResponseDto)] = {
    import VrmAssignFulfilServiceImpl.ServiceName
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) {
        healthStats.success(HealthStatsSuccess(ServiceName, dateService.now))
        (resp.status, resp.json.as[VrmAssignFulfilResponseDto])
      }
      else if (resp.status == Status.INTERNAL_SERVER_ERROR) {
        val msg = s"Vrm Assign Fulfil micro-service call http status not OK, it was: ${resp.status}"
        val error = new RuntimeException(msg)
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, error))
        (resp.status, resp.json.as[VrmAssignFulfilResponseDto])
      }
      else {
        val error = new RuntimeException(
          "Vrm Assign Fulfil micro service call http status not OK, it " +
            s"was: ${resp.status}. Problem may come from either vrm-assign-fulfil micro-service or VSS"
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

object VrmAssignFulfilServiceImpl {
  final val ServiceName = "vrm-assign-fulfil-microservice"
}