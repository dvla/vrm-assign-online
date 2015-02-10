package composition.webserviceclients.emailservice

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.emailservice.TestEmailServiceWebServiceBinding.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import webserviceclients.emailservice.{EmailServiceSendRequest, EmailServiceWebService, EmailServiceSendResponse}
import webserviceclients.fakes.EmailServiceWebServiceConstants._
import webserviceclients.fakes._

import scala.concurrent.Future

final class TestEmailServiceWebServiceBinding(emailServiceWebService: EmailServiceWebService = mock(classOf[EmailServiceWebService]), // This can be passed in so the calls to the mock can be verified
                                              statusAndResponse: (Int, Option[EmailServiceSendResponse]) = emailServiceSendResponseSuccess
                                               ) extends ScalaModule with MockitoSugar {

  val stub = {
    when(emailServiceWebService.invoke(any[EmailServiceSendRequest])).thenReturn(Future.successful(createResponse(statusAndResponse)))
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