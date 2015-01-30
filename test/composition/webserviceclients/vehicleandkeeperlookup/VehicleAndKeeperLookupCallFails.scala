package composition.webserviceclients.vehicleandkeeperlookup

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperLookupWebService}

import scala.concurrent.Future

final class VehicleAndKeeperLookupCallFails extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[VehicleAndKeeperLookupWebService]
    when(webService.invoke(any[VehicleAndKeeperDetailsRequest], any[String])).
      thenReturn(Future.failed(new RuntimeException("This error is generated deliberately by a stub for VehicleAndKeeperLookupWebService")))
    webService
  }

  def configure() = bind[VehicleAndKeeperLookupWebService].toInstance(stub)
}
