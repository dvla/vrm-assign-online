package composition.webserviceclients.vrmassigneligibility

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vrmassigneligibility.TestVrmAssignEligibilityWebServiceBinding.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import scala.concurrent.Future
import webserviceclients.fakes.VrmAssignEligibilityWebServiceConstants.vrmAssignEligibilityResponseDirectToPaperError
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityRequest
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityWebService

final class VrmAssignEligibilityCallDirectToPaperError extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[VrmAssignEligibilityWebService]
    when(webService.invoke(any[VrmAssignEligibilityRequest], any[String])).
      thenReturn(Future.successful(createResponse(vrmAssignEligibilityResponseDirectToPaperError)))
    webService
  }

  def configure() = bind[VrmAssignEligibilityWebService].toInstance(stub)
}