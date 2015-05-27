package composition.webserviceclients.audit2

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.BAD_REQUEST
import scala.concurrent.Future
import webserviceclients.audit2.AuditMicroService
import webserviceclients.audit2.AuditRequest
import webserviceclients.fakes.FakeResponse

final class AuditMicroServiceCallNotOk extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[AuditMicroService]
    when(webService.invoke(request = any[AuditRequest])).
      thenReturn(Future.successful(new FakeResponse(status = BAD_REQUEST)))
    webService
  }

  def configure() = bind[AuditMicroService].toInstance(stub)
}
