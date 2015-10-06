package composition.webserviceclients.vrmassigneligibility

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vrmassigneligibility.TestVrmAssignEligibilityWebServiceBinding.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import webserviceclients.fakes.FakeResponse
import webserviceclients.fakes.VrmAssignEligibilityWebServiceConstants.vrmAssignEligibilityResponseSuccess
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityRequest
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityResponseDto
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityWebService

final class TestVrmAssignEligibilityWebServiceBinding(
            statusAndResponse: (Int, VrmAssignEligibilityResponseDto) = vrmAssignEligibilityResponseSuccess)
  extends ScalaModule with MockitoSugar {

  val stub = {
    val webService: VrmAssignEligibilityWebService = mock[VrmAssignEligibilityWebService]
    when(webService.invoke(any[VrmAssignEligibilityRequest],
      any[TrackingId]))
      .thenReturn(Future.successful(createResponse(statusAndResponse)))
    webService
  }

  def configure() = bind[VrmAssignEligibilityWebService].toInstance(stub)
}

object TestVrmAssignEligibilityWebServiceBinding {

  def createResponse(
                 response: (Int, VrmAssignEligibilityResponseDto)) = {
    val (status, dto) = response
    val asJson = Json.toJson(dto)
    new FakeResponse(status = status, fakeJson = Some(asJson))
  }
}