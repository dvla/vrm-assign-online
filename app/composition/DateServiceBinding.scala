package composition

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.services.{DateServiceImpl, DateService}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperLookupService, VehicleAndKeeperLookupServiceImpl}

final class DateServiceBinding extends ScalaModule {

  def configure() = {
    bind[DateService].to[DateServiceImpl].asEagerSingleton()
  }
}
