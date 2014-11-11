package webserviceclients.vrmassignfulfil

import scala.concurrent.Future

trait VrmAssignFulfilService {

  def invoke(cmd: VrmAssignFulfilRequest,
             trackingId: String): Future[VrmAssignFulfilResponse]
}