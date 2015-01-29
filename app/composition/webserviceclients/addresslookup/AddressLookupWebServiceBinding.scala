package composition.webserviceclients.addresslookup

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.AddressLookupWebService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.WebServiceImpl

final class AddressLookupWebServiceBinding extends ScalaModule {

  def configure() = {
    bind[AddressLookupWebService].to[WebServiceImpl].asEagerSingleton()
  }
}