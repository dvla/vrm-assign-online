package composition.webserviceclients.vrmassigneligibility

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vrmassigneligibility.TestVrmAssignEligibilityWebServiceBinding.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import webserviceclients.fakes.FakeResponse
import webserviceclients.fakes.VrmAssignEligibilityWebServiceConstants._
import webserviceclients.vrmretentioneligibility.{VrmAssignEligibilityRequest, VrmAssignEligibilityResponse, VrmAssignEligibilityWebService}

import scala.concurrent.Future

final class TestVrmAssignEligibilityWebServiceBinding(
                                                       statusAndResponse: (Int, Option[VrmAssignEligibilityResponse]) = vrmAssignEligibilityResponseSuccess
                                                       ) extends ScalaModule with MockitoSugar {

  val stub = {
    val webService: VrmAssignEligibilityWebService = mock[VrmAssignEligibilityWebService]
    when(webService.invoke(any[VrmAssignEligibilityRequest], any[String])).thenReturn(Future.successful(createResponse(statusAndResponse)))
    webService
  }

  def configure = bind[VrmAssignEligibilityWebService].toInstance(stub)
}

object TestVrmAssignEligibilityWebServiceBinding {

  def createResponse(response: (Int, Option[VrmAssignEligibilityResponse])) = {
    val (status, dto) = response
    val asJson = Json.toJson(dto)
    new FakeResponse(status = status, fakeJson = Some(asJson))
  }
}