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
import webserviceclients.fakes.VrmAssignEligibilityWebServiceConstants
import VrmAssignEligibilityWebServiceConstants.vrmAssignEligibilityResponseSuccess
import VrmAssignEligibilityWebServiceConstants.vrmAssignEligibilityResponseDirectToPaperError
import VrmAssignEligibilityWebServiceConstants.vrmAssignEligibilityResponseExpiredCertOver6Years
import VrmAssignEligibilityWebServiceConstants.vrmAssignEligibilityResponseExpiredCertWithin6Years
import VrmAssignEligibilityWebServiceConstants.vrmAssignEligibilityResponseNotEligibleError
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityRequest
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityResponseDto
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityWebService

sealed class TestVrmAssignEligibilityWebServiceBinding(
            statusAndResponse: (Int, VrmAssignEligibilityResponseDto)) extends ScalaModule with MockitoSugar {

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

final class VrmAssignEligibilityCallSuccess extends
  TestVrmAssignEligibilityWebServiceBinding(vrmAssignEligibilityResponseSuccess)

final class VrmAssignEligibilityCallDirectToPaperError extends
  TestVrmAssignEligibilityWebServiceBinding(vrmAssignEligibilityResponseDirectToPaperError)

final class VrmAssignEligibilityCallExpiredCertOver6Years extends
  TestVrmAssignEligibilityWebServiceBinding(vrmAssignEligibilityResponseExpiredCertOver6Years)

final class VrmAssignEligibilityCallExpiredCertWithin6Years extends
  TestVrmAssignEligibilityWebServiceBinding(vrmAssignEligibilityResponseExpiredCertWithin6Years)

final class VrmAssignEligibilityCallNotEligibleError extends
  TestVrmAssignEligibilityWebServiceBinding(vrmAssignEligibilityResponseNotEligibleError)
