package webserviceclients.audit2

import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import scala.concurrent.Future

trait AuditService {

  def send(auditRequest: AuditRequest, trackingId: TrackingId): Future[Unit]
}