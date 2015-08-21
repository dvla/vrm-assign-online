package webserviceclients.audit2

import play.api.mvc.Request
import scala.concurrent.Future

trait AuditService {

  def send(auditRequest: AuditRequest)
          (implicit request: Request[_]): Future[Unit]
}