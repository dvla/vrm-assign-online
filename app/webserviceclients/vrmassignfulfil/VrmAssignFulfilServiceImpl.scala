package webserviceclients.vrmassignfulfil

import javax.inject.Inject

import play.api.http.Status

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class VrmAssignFulfilServiceImpl @Inject()(ws: VrmAssignFulfilWebService)
  extends VrmAssignFulfilService {

  override def invoke(cmd: VrmAssignFulfilRequest,
                      trackingId: String): Future[VrmAssignFulfilResponse] = {
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) resp.json.as[VrmAssignFulfilResponse]
      else throw new RuntimeException(
        s"Vrm Assign Fulfil web service call http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either vrm-assign-fulfil micro-service or the VSS"
      )
    }.recover {
      case NonFatal(e) => throw new RuntimeException("Vrm Assign Fulfil call failed for an unknown reason", e)
    }
  }
}