package composition.webserviceclients.vehicleandkeeperlookup

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebServiceBinding.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperDetailsResponse, VehicleAndKeeperLookupWebService}
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._
import webserviceclients.fakes._

import scala.concurrent.Future

final class TestVehicleAndKeeperLookupWebServiceBinding(
                                                         vehicleAndKeeperLookupWebService: VehicleAndKeeperLookupWebService = mock(classOf[VehicleAndKeeperLookupWebService]), // This can be passed in so the calls to the mock can be verified
                                                         statusAndResponse: (Int, Option[VehicleAndKeeperDetailsResponse]) = vehicleAndKeeperDetailsResponseSuccess
                                                         ) extends ScalaModule with MockitoSugar {

  def build() = {
    when(vehicleAndKeeperLookupWebService.invoke(any[VehicleAndKeeperDetailsRequest], any[String])).thenReturn(Future.successful(createResponse(statusAndResponse)))
    vehicleAndKeeperLookupWebService
  }

  def configure() = {

    bind[VehicleAndKeeperLookupWebService].toInstance(build())
  }
}

object TestVehicleAndKeeperLookupWebServiceBinding {

  def createResponse(response: (Int, Option[VehicleAndKeeperDetailsResponse])) = {
    val (status, dto) = response
    val asJson = Json.toJson(dto)
    new FakeResponse(status = status, fakeJson = Some(asJson))
  }
}