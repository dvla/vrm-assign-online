package composition.vrmassigneligibility

import com.tzavellas.sse.guice.ScalaModule
import composition.vrmassigneligibility.TestVrmAssignEligibilityWebService.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import webserviceclients.fakes.VrmAssignEligibilityWebServiceConstants.vrmAssignEligibilityResponseDirectToPaperError
import webserviceclients.vrmretentioneligibility.{VrmAssignEligibilityWebService, VrmAssignEligibilityRequest}

import scala.concurrent.Future

final class VrmAssignEligibilityCallDirectToPaperError extends ScalaModule with MockitoSugar {

  def configure() = {
    val vrmAssignEligibilityWebService = mock[VrmAssignEligibilityWebService]
    when(vrmAssignEligibilityWebService.invoke(any[VrmAssignEligibilityRequest], any[String])).
      thenReturn(Future.successful(createResponse(vrmAssignEligibilityResponseDirectToPaperError)))
    bind[VrmAssignEligibilityWebService].toInstance(vrmAssignEligibilityWebService)
  }

}