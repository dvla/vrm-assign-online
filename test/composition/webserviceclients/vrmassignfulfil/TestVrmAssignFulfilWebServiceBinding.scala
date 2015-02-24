package composition.webserviceclients.vrmassignfulfil

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vrmassigneligibility.TestVrmAssignEligibilityWebServiceBinding.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import webserviceclients.fakes.VrmAssignEligibilityWebServiceConstants.vrmAssignEligibilityResponseDirectToPaperError
import webserviceclients.vrmassignfulfil.VrmAssignFulfilRequest
import webserviceclients.vrmassignfulfil.VrmAssignFulfilWebService
import webserviceclients.vrmretentioneligibility.VrmAssignEligibilityRequest
import webserviceclients.vrmretentioneligibility.VrmAssignEligibilityWebService

import scala.concurrent.Future

final class TestVrmAssignFulfilWebServiceBinding extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[VrmAssignFulfilWebService]
//    when(webService.invoke(any[VrmAssignFulfilRequest], any[String])).
//      thenReturn(Future.successful(createResponse(vrmAssignEligibilityResponseDirectToPaperError)))
    webService
  }

  def configure() = bind[VrmAssignFulfilWebService].toInstance(stub)
}