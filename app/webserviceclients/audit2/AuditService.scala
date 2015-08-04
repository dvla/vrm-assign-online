package webserviceclients.audit2

import play.api.mvc.Request
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory

import scala.concurrent.Future

trait AuditService {

  def send(auditRequest: AuditRequest)
          (implicit request: Request[_]): Future[Unit]
}