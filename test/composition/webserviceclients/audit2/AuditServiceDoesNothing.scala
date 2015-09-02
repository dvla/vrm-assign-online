package composition.webserviceclients.audit2

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.mvc.Request
import scala.concurrent.Future
import webserviceclients.audit2.AuditRequest
import webserviceclients.audit2.AuditService

final class AuditServiceDoesNothing extends ScalaModule with MockitoSugar {

  val stub = {
    val service = mock[AuditService]
    when(service.send(auditRequest = any[AuditRequest])(request = any[Request[_]])).thenReturn(Future.successful {})
    service
  }

  def configure() = bind[AuditService].toInstance(stub)
}
