package composition.webserviceclients.vrmassignfulfil

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vrmassignfulfil.TestVrmAssignEligibilityWebServiceBinding.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import scala.concurrent.Future
import webserviceclients.fakes.FakeResponse
import webserviceclients.fakes.VrmAssignFulfilWebServiceConstants.vrmAssignFulfilResponseSuccess
import webserviceclients.vrmassignfulfil.VrmAssignFulfilRequest
import webserviceclients.vrmassignfulfil.VrmAssignFulfilResponseDto
import webserviceclients.vrmassignfulfil.VrmAssignFulfilWebService

final class TestVrmAssignFulfilWebServiceBinding extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[VrmAssignFulfilWebService]
    when(webService.invoke(any[VrmAssignFulfilRequest], any[TrackingId]))
      .thenReturn(Future.successful(createResponse(vrmAssignFulfilResponseSuccess)))
    webService
  }

  def configure() = bind[VrmAssignFulfilWebService].toInstance(stub)
}

object TestVrmAssignEligibilityWebServiceBinding {

  def createResponse(response: (Int, VrmAssignFulfilResponseDto)) = {
    val (status, dto) = response
    val asJson = Json.toJson(dto)
    new FakeResponse(status = status, fakeJson = Some(asJson))
  }
}
