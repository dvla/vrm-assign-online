package composition.webserviceclients.vehicleandkeeperlookup

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebServiceBinding.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, when}
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.{Json, JsValue}
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup
import vehicleandkeeperlookup.VehicleAndKeeperLookupFailureResponse
import vehicleandkeeperlookup.VehicleAndKeeperLookupRequest
import vehicleandkeeperlookup.VehicleAndKeeperLookupSuccessResponse
import vehicleandkeeperlookup.VehicleAndKeeperLookupWebService
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseSuccess
import webserviceclients.fakes.FakeResponse

final class TestVehicleAndKeeperLookupWebServiceBinding(
  // This can be passed in so the calls to the mock can be verified
  vehicleAndKeeperLookupWebService: VehicleAndKeeperLookupWebService = mock(classOf[VehicleAndKeeperLookupWebService]),
  statusAndResponse: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse, VehicleAndKeeperLookupSuccessResponse]])
  = vehicleAndKeeperDetailsResponseSuccess
  ) extends ScalaModule with MockitoSugar {

  val stub = {
    when(vehicleAndKeeperLookupWebService.invoke(any[VehicleAndKeeperLookupRequest], any[TrackingId]))
      .thenReturn(Future.successful(createResponse(statusAndResponse)))
    vehicleAndKeeperLookupWebService
  }

  def configure() = bind[VehicleAndKeeperLookupWebService].toInstance(stub)
}

object TestVehicleAndKeeperLookupWebServiceBinding {

  def createResponse(
    response: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse, VehicleAndKeeperLookupSuccessResponse]])) = {
    val (status: Int, dto: Option[JsValue]) = response match {
      case (s, None) => (s, None)
      case (s, Some(Left(failure))) => (s, Some(Json.toJson(failure)))
      case (s, Some(Right(success))) => (s, Some(Json.toJson(success)))
    }
    new FakeResponse(status = status, fakeJson = dto)
  }
}