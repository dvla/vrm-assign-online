package composition

import com.tzavellas.sse.guice.ScalaModule
import email.AssignEmailService
import email.AssignEmailServiceImpl
import org.scalatest.mock.MockitoSugar

final class TestAssignEmailServiceBinding extends ScalaModule with MockitoSugar {

  val stub = mock[AssignEmailService]

  def configure() = {
    bind[AssignEmailService].toInstance(stub)
  }
}