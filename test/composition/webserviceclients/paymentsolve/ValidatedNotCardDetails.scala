package composition.webserviceclients.paymentsolve

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.paymentsolve.TestPaymentWebServiceBinding.beginResponseWithValidDefaults
import composition.webserviceclients.paymentsolve.TestPaymentWebServiceBinding.invalidStatus
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import scala.concurrent.Future
import webserviceclients.fakes.FakeResponse
import webserviceclients.paymentsolve.{PaymentSolveBeginRequest, PaymentSolveWebService}

final class ValidatedNotCardDetails extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveBeginRequest], tracking = any[String])).
      thenReturn(Future.successful(
        new FakeResponse(status = OK, fakeJson = beginResponseWithValidDefaults(status = invalidStatus)))
      )
    webService
  }

  def configure() = bind[PaymentSolveWebService].toInstance(stub)
}
