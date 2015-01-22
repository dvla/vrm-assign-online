package composition.audit2

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import webserviceclients.audit2.{AuditRequest, AuditService}

import scala.concurrent.Future

final class AuditServiceReal extends ScalaModule with MockitoSugar {

  def configure() = {
    bind[webserviceclients.audit2.AuditService].to[webserviceclients.audit2.AuditServiceImpl].asEagerSingleton()
  }
}
