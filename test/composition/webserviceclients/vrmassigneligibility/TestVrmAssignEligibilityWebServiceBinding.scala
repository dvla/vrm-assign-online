package composition.webserviceclients.vrmassigneligibility

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vrmassigneligibility.TestVrmAssignEligibilityWebServiceBinding.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import scala.concurrent.Future
import webserviceclients.fakes.FakeResponse
import webserviceclients.fakes.VrmAssignEligibilityWebServiceConstants.vrmAssignEligibilityResponseSuccess
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityRequest
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityResponse
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityWebService

final class TestVrmAssignEligibilityWebServiceBinding(statusAndResponse: (Int, Option[VrmAssignEligibilityResponse]) = vrmAssignEligibilityResponseSuccess)
  extends ScalaModule with MockitoSugar {

  val stub = {
    val webService: VrmAssignEligibilityWebService = mock[VrmAssignEligibilityWebService]
    when(webService.invoke(any[VrmAssignEligibilityRequest],
      any[String]))
      .thenReturn(Future.successful(createResponse(statusAndResponse)))
    webService
  }

  def configure() = bind[VrmAssignEligibilityWebService].toInstance(stub)
}

object TestVrmAssignEligibilityWebServiceBinding {

  def createResponse(response: (Int, Option[VrmAssignEligibilityResponse])) = {
    val (status, dto) = response
    val asJson = Json.toJson(dto)
    new FakeResponse(status = status, fakeJson = Some(asJson))
  }
}