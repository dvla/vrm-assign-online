package controllers

import helpers.UnitSpec
import helpers.vrm_assign.CookieFactoryForUnitSpecs
import helpers.TestWithApplication
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.NoCookieFlags
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.AddressLookupService

class AddressLookupUnitSpec extends UnitSpec {
  private class AddressLookupTest(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                          addressLookup: AddressLookupService) extends AddressLookup {
    override def authenticate(request: Request[_]) = super.authenticate(request)
  }

  "authenticate" should {
    implicit val cookieFlags = new NoCookieFlags()
    implicit val clientSideSessionFactory = new ClearTextClientSideSessionFactory()
    implicit val addressLookup = mock[AddressLookupService]

    "return true if VehicleAndKeeperDetailsModel is set" in new TestWithApplication {
      val request = FakeRequest().withCookies(
        CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()
      )
      new AddressLookupTest().authenticate(request) should equal(true)
    }

    "return false if VehicleAndKeeperDetailsModel is set" in new TestWithApplication {
      new AddressLookupTest().authenticate(FakeRequest()) should equal(false)
    }




  }
}
