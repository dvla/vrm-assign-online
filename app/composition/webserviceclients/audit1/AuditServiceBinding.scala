package composition.webserviceclients.audit1

import com.tzavellas.sse.guice.ScalaModule

final class AuditServiceBinding extends ScalaModule {

  def configure() = {
    bind[audit1.AuditService].to[audit1.AuditLocalServiceImpl].asEagerSingleton()
  }
}