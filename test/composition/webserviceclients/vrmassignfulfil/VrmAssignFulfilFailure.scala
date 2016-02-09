package composition.webserviceclients.vrmassignfulfil

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vrmassignfulfil.TestVrmAssignEligibilityWebServiceBinding.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import webserviceclients.fakes.VrmAssignFulfilWebServiceConstants.vrmAssignFulfilResponseFailure
import webserviceclients.vrmassignfulfil.VrmAssignFulfilRequest
import webserviceclients.vrmassignfulfil.VrmAssignFulfilWebService

final class VrmAssignFulfilFailure extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[VrmAssignFulfilWebService]
    when(webService.invoke(any[VrmAssignFulfilRequest], any[TrackingId]))
      .thenReturn(Future.successful(createResponse(vrmAssignFulfilResponseFailure)))
    webService
  }

  def configure() = bind[VrmAssignFulfilWebService].toInstance(stub)
}
