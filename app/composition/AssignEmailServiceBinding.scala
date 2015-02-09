package composition

import com.tzavellas.sse.guice.ScalaModule
import email.{AssignEmailServiceImpl, AssignEmailService}

final class AssignEmailServiceBinding extends ScalaModule {

  def configure() = {
    bind[AssignEmailService].to[AssignEmailServiceImpl].asEagerSingleton()
  }
}