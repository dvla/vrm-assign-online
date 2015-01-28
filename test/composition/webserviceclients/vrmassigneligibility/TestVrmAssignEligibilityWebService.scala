package composition.webserviceclients.vrmassigneligibility

import TestVrmAssignEligibilityWebService.createResponse
import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import webserviceclients.fakes.FakeResponse
import webserviceclients.fakes.VrmAssignEligibilityWebServiceConstants._
import webserviceclients.vrmretentioneligibility.{VrmAssignEligibilityRequest, VrmAssignEligibilityWebService, VrmAssignEligibilityResponse}

import scala.concurrent.Future

final class TestVrmAssignEligibilityWebService(vrmAssignEligibilityWebService: VrmAssignEligibilityWebService = mock(classOf[VrmAssignEligibilityWebService]), // This can be passed in so the calls to the mock can be verified
                                            statusAndResponse: (Int, Option[VrmAssignEligibilityResponse]) = vrmAssignEligibilityResponseSuccess
                                            ) extends ScalaModule with MockitoSugar {

  def configure() = {
    when(vrmAssignEligibilityWebService.invoke(any[VrmAssignEligibilityRequest], any[String])).thenReturn(Future.successful(createResponse(statusAndResponse)))
    bind[VrmAssignEligibilityWebService].toInstance(vrmAssignEligibilityWebService)
  }
}

object TestVrmAssignEligibilityWebService {

  def createResponse(response: (Int, Option[VrmAssignEligibilityResponse])) = {
    val (status, dto) = response
    val asJson = Json.toJson(dto)
    new FakeResponse(status = status, fakeJson = Some(asJson))
  }
}