package composition

import com.tzavellas.sse.guice.ScalaModule

final class AuditService extends ScalaModule {

  def configure() = {
    bind[webserviceclients.audit2.AuditService].to[webserviceclients.audit2.AuditServiceImpl].asEagerSingleton()
  }
}
