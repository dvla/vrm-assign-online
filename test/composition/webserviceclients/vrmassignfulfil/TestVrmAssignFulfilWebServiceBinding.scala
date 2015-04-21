package composition.webserviceclients.vrmassignfulfil

import com.tzavellas.sse.guice.ScalaModule
import org.scalatest.mock.MockitoSugar
import webserviceclients.vrmassignfulfil.VrmAssignFulfilWebService

final class TestVrmAssignFulfilWebServiceBinding extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[VrmAssignFulfilWebService]
//    when(webService.invoke(any[VrmAssignFulfilRequest], any[String])).
//      thenReturn(Future.successful(createResponse(vrmAssignEligibilityResponseDirectToPaperError)))
    webService
  }

  def configure() = bind[VrmAssignFulfilWebService].toInstance(stub)
}