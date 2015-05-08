package composition.webserviceclients.vehicleandkeeperlookup

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebServiceBinding.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, when}
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupRequest
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupResponse
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupWebService
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseSuccess
import webserviceclients.fakes.FakeResponse

final class TestVehicleAndKeeperLookupWebServiceBinding(vehicleAndKeeperLookupWebService: VehicleAndKeeperLookupWebService
                                                          = mock(classOf[VehicleAndKeeperLookupWebService]), // This can be passed in so the calls to the mock can be verified
                                                        statusAndResponse: (Int, Option[VehicleAndKeeperLookupResponse])
                                                          = vehicleAndKeeperDetailsResponseSuccess
                                                       ) extends ScalaModule with MockitoSugar {

  val stub = {
    when(vehicleAndKeeperLookupWebService.invoke(any[VehicleAndKeeperLookupRequest], any[String]))
      .thenReturn(Future.successful(createResponse(statusAndResponse)))
    vehicleAndKeeperLookupWebService
  }

  def configure() = bind[VehicleAndKeeperLookupWebService].toInstance(stub)
}

object TestVehicleAndKeeperLookupWebServiceBinding {

  def createResponse(response: (Int, Option[VehicleAndKeeperLookupResponse])) = {
    val (status, dto) = response
    val asJson = Json.toJson(dto)
    new FakeResponse(status = status, fakeJson = Some(asJson))
  }
}