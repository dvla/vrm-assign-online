package composition.addresslookup

import _root_.webserviceclients.fakes.AddressLookupServiceConstants.PostcodeInvalid
import _root_.webserviceclients.fakes.AddressLookupWebServiceConstants
import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.matches
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Lang
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.AddressLookupWebService

final class TestAddressLookupWebServiceBinding extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[AddressLookupWebService]
    when(webService.callPostcodeWebService(matches(AddressLookupWebServiceConstants.traderUprnValid.toString),
      trackingId = any[TrackingId])(any[Lang]))
      .thenReturn(AddressLookupWebServiceConstants.responseValidForUprnToAddress)
    when(webService.callPostcodeWebService(matches(AddressLookupWebServiceConstants.traderUprnInvalid.toString),
      trackingId = any[TrackingId])(any[Lang]))
      .thenReturn(AddressLookupWebServiceConstants.responseValidForUprnToAddressNotFound)
    webService
  }

  def configure() = bind[AddressLookupWebService].toInstance(stub)
}