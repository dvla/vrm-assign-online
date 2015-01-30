package composition.webserviceclients.vrmassigneligibility

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vrmassigneligibility.TestVrmAssignEligibilityWebServiceBinding.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import webserviceclients.fakes.VrmAssignEligibilityWebServiceConstants.vrmAssignEligibilityResponseNotEligibleError
import webserviceclients.vrmretentioneligibility.{VrmAssignEligibilityRequest, VrmAssignEligibilityWebService}

import scala.concurrent.Future

final class VrmAssignEligibilityCallNotEligibleError extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[VrmAssignEligibilityWebService]
    when(webService.invoke(any[VrmAssignEligibilityRequest], any[String])).
      thenReturn(Future.successful(createResponse(vrmAssignEligibilityResponseNotEligibleError)))
    webService
  }
  def configure() = bind[VrmAssignEligibilityWebService].toInstance(stub)
}