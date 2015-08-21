package composition.webserviceclients.addresslookup

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.AddressLookupService

final class AddressLookupServiceBinding extends ScalaModule {

  def configure() = {
    bind[AddressLookupService].
      to[uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl]
  }
}