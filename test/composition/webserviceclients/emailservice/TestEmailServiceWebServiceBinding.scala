package composition.webserviceclients.emailservice

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.emailservice.TestEmailServiceWebServiceBinding.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, when}
import org.scalatest.mock.MockitoSugar
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailServiceSendRequest
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailServiceSendResponse
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailServiceWebService
import webserviceclients.fakes.EmailServiceWebServiceConstants.emailServiceSendResponseSuccess
import webserviceclients.fakes.FakeResponse

final class TestEmailServiceWebServiceBinding(emailServiceWebService: EmailServiceWebService = mock(classOf[EmailServiceWebService]), // This can be passed in so the calls to the mock can be verified
                                              statusAndResponse: (Int, Option[EmailServiceSendResponse]) = emailServiceSendResponseSuccess
                                               ) extends ScalaModule with MockitoSugar {

  val stub = {
    when(emailServiceWebService.invoke(any[EmailServiceSendRequest], any[TrackingId])).thenReturn(
      Future.successful(createResponse(statusAndResponse))
    )
    emailServiceWebService
  }

  def configure() = bind[EmailServiceWebService].toInstance(stub)
}

object TestEmailServiceWebServiceBinding {

  def createResponse(response: (Int, Option[EmailServiceSendResponse])) = {
    val (status, respOpt) = response
    new FakeResponse(status = status)
  }
}