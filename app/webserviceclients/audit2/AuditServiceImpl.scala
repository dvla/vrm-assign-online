package webserviceclients.audit2

import com.google.inject.Inject
import play.api.http.Status
import play.api.mvc.Request
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import utils.helpers.Config

class AuditServiceImpl @Inject()(config: Config, ws: AuditMicroService)
                                (implicit clientSideSessionFactory: ClientSideSessionFactory)
  extends AuditService with DVLALogger {

  override def send(auditRequest: AuditRequest)
                   (implicit request: Request[_]): Future[Unit] = {
    if (config.auditMicroServiceUrlBase == "NOT FOUND")
      Future.successful(logMessage(request.cookies.trackingId, Info, "auditMicroServiceUrlBase not set in config"))
    else ws.invoke(auditRequest).map { resp =>
      if (resp.status == Status.OK) {
        // Do nothing, it's a fire-and forget
      }
      else {
        logMessage(request.cookies.trackingId, Error,
          s"Audit micro-service call http status not OK, it was: ${resp.status}")
        throw new RuntimeException(s"Audit micro-service call http status not OK, it was: ${resp.status}")
      }
    }.recover {
      case NonFatal(e) =>
        logMessage(request.cookies.trackingId, Error, s"Audit call failed for an unknown reason: $e")
        throw new RuntimeException(s"Audit call failed for an unknown reason: $e")
    }
  }
}