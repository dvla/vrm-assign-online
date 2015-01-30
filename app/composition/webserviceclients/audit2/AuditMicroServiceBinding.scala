package composition.webserviceclients.audit2

import com.tzavellas.sse.guice.ScalaModule

final class AuditMicroServiceBinding extends ScalaModule {

  def configure() = {
    bind[_root_.webserviceclients.audit2.AuditMicroService].to[_root_.webserviceclients.audit2.AuditMicroServiceImpl].asEagerSingleton()
  }
}