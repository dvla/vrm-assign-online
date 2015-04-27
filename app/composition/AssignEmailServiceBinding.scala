package composition

import com.tzavellas.sse.guice.ScalaModule
import email.AssignEmailService
import email.AssignEmailServiceImpl

final class AssignEmailServiceBinding extends ScalaModule {

  def configure() = {
    bind[AssignEmailService].to[AssignEmailServiceImpl].asEagerSingleton()
  }
}