package composition.webserviceclients.audit2

import com.tzavellas.sse.guice.ScalaModule

final class AuditMicroServiceBinding extends ScalaModule {

  def configure() = {
    bind[_root_.webserviceclients.audit2.AuditService].to[_root_.webserviceclients.audit2.AuditServiceImpl].asEagerSingleton()
  }
}