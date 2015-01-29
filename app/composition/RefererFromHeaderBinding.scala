package composition

import com.tzavellas.sse.guice.ScalaModule

final class RefererFromHeaderBinding extends ScalaModule {

  def configure() = {
    bind[RefererFromHeader].to[RefererFromHeaderImpl].asEagerSingleton()
  }
}
