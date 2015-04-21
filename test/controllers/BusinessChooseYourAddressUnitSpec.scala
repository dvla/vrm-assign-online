package controllers

import composition.webserviceclients.audit2.AuditServiceDoesNothing
import composition.TestConfig
import composition.WithApplication
import controllers.Common.PrototypeHtml
import helpers.UnitSpec
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_assign.CookieFactoryForUnitSpecs
import org.mockito.Mockito._
import pages.vrm_assign.ConfirmBusinessPage
import pages.vrm_assign.SetupBusinessDetailsPage
import pages.vrm_assign.UprnNotFoundPage
import play.api.mvc.Cookies
import play.api.test.FakeRequest
import play.api.test.Helpers.BAD_REQUEST
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.OK
import play.api.test.Helpers.SET_COOKIE
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers._
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import views.vrm_assign.BusinessChooseYourAddress.AddressSelectId
import views.vrm_assign.BusinessChooseYourAddress.BusinessChooseYourAddressCacheKey
import views.vrm_assign.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_assign.EnterAddressManually.EnterAddressManuallyCacheKey
import webserviceclients.audit2.AuditRequest
import webserviceclients.fakes.AddressLookupWebServiceConstants
import webserviceclients.fakes.AddressLookupWebServiceConstants.traderUprnInvalid
import webserviceclients.fakes.AddressLookupWebServiceConstants.traderUprnValid

final class BusinessChooseYourAddressUnitSpec extends UnitSpec {

  "present (use UPRN enabled)" should {

    "display the page if dealer details cached" in new WithApplication {
      whenReady(present(ordnanceSurveyUseUprn = true), timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "display expected drop-down values" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="$traderUprnValid" >""")
    }

    "display selected field when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddressUseUprn()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).present(request)
      val content = contentAsString(result)
      content should include( s"""<option value="$traderUprnValid" selected>""")
    }

    "display unselected field when cookie does not exist" in new WithApplication {
      val content = contentAsString(present(ordnanceSurveyUseUprn = true))
      content should not include "selected"
    }

    "redirect to setupTradeDetails page when present with no business details cached" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present(ordnanceSurveyUseUprn = true)) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddressPrototypeBannerNotVisible(ordnanceSurveyUseUprn = true).present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "present (use UPRN not enabled for Northern Ireland)" should {

    "display the page if dealer details cached" in new WithApplication {
      whenReady(present(ordnanceSurveyUseUprn = false), timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "display expected drop-down values" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="0" >""")
    }

    "display selected field when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddress()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).present(request)
      val content = contentAsString(result)
      content should include( s"""<option value="0" selected>""")
    }

    "display unselected field when cookie does not exist" in new WithApplication {
      val content = contentAsString(present(ordnanceSurveyUseUprn = false))
      content should not include "selected"
    }

    "redirect to setupTradeDetails page when present with no business details cached" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present(ordnanceSurveyUseUprn = false)) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddressPrototypeBannerNotVisible(ordnanceSurveyUseUprn = false).present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit (use UPRN enabled)" should {

    "redirect to Confirm Business page after a valid submit" in new WithApplication {
      val auditService2 = new AuditServiceDoesNothing

      val injector = testInjector(
        new TestConfig(ordnanceSurveyUseUprn = true),
        auditService2
      )

      val businessChooseYourAddress = injector.getInstance(classOf[BusinessChooseYourAddress])
      val dateService = injector.getInstance(classOf[DateService])

      val data = Seq(("transactionId", "ABC123123123123"),
        ("timestamp", dateService.dateTimeISOChronology),
        ("currentVrm", "AB12AWR"),
        ("make", "Alfa Romeo"),
        ("model", "Alfasud ti"),
        ("keeperName", "MR DAVID JONES"),
        ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA"),
        ("businessName", "example trader contact"),
        ("businessAddress", "example trader name, business line1 stub, business line2 stub, business postTown stub, QQ99QQ"),
        ("businessEmail", "business.example@test.com"))
      val auditRequest = new AuditRequest(AuditRequest.CaptureActorToConfirmBusiness, AuditRequest.AuditServiceType, data)
      val request = buildCorrectlyPopulatedRequest(addressSelected = traderUprnValid.toString).
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmBusinessPage.address))
        verify(auditService2.stub).send(auditRequest)
      }
    }

    "return a bad request if not address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "display expected drop-down values when no address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="$traderUprnValid" >""")
    }

    "redirect to SetupBusinessDetailsPage page when valid submit with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = traderUprnValid.toString).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "redirect to SetupBusinessDetailsPage page when bad form submitted and no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
      // Bad form because nothing was selected from the drop-down.
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "redirect to UprnNotFound page when submit with but uprn not found by the webservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = traderUprnInvalid.toString).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(UprnNotFoundPage.address))
      }
    }

    "write cookie when uprn found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = traderUprnValid.toString).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(BusinessChooseYourAddressCacheKey,
          BusinessDetailsCacheKey,
          EnterAddressManuallyCacheKey)
      }
    }

    "does not write cookie when uprn not found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = AddressLookupWebServiceConstants.traderUprnInvalid.toString).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        val cookies = r.header.headers.get(SET_COOKIE).toSeq.flatMap(Cookies.decode)
        cookies.map(_.name) should contain noneOf(BusinessChooseYourAddressCacheKey, BusinessDetailsCacheKey)
      }
    }
  }

  "submit (use UPRN not enabled for Northern Ireland)" should {

    "redirect to Confirm page after a valid submit" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmBusinessPage.address))
      }
    }

    "return a bad request if not address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "display expected drop-down values when no address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="0" >""")
    }

    "redirect to SetupBusinessDetailsPage page when valid submit with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "redirect to SetupBusinessDetailsPage page when bad form submitted and no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
      // Bad form because nothing was selected from the drop-down.
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "redirect to UprnNotFound page when submit with but uprn not found by the webservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = traderUprnInvalid.toString).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(UprnNotFoundPage.address))
      }
    }

    "write cookie when uprn found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(BusinessChooseYourAddressCacheKey,
          BusinessDetailsCacheKey,
          EnterAddressManuallyCacheKey)
      }
    }

    "does not write cookie when uprn not found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = AddressLookupWebServiceConstants.traderUprnInvalid.toString).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        val cookies = r.header.headers.get(SET_COOKIE).toSeq.flatMap(Cookies.decode)
        cookies.map(_.name) should contain noneOf(BusinessChooseYourAddressCacheKey, BusinessDetailsCacheKey)
      }
    }
  }

  private def present(ordnanceSurveyUseUprn: Boolean) = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
      withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
    businessChooseYourAddress(ordnanceSurveyUseUprn = ordnanceSurveyUseUprn).present(request)
  }

  private def businessChooseYourAddress(ordnanceSurveyUseUprn: Boolean) = {
    testInjector(
      new TestConfig(ordnanceSurveyUseUprn = ordnanceSurveyUseUprn)
    ).getInstance(classOf[BusinessChooseYourAddress])
  }

  private def businessChooseYourAddressPrototypeBannerNotVisible(ordnanceSurveyUseUprn: Boolean) = {
    testInjector(
      new TestConfig(isPrototypeBannerVisible = false, ordnanceSurveyUseUprn = ordnanceSurveyUseUprn)
    ).getInstance(classOf[BusinessChooseYourAddress])
  }

  private def buildCorrectlyPopulatedRequest(addressSelected: String = "0") = {
    FakeRequest().withFormUrlEncodedBody(
      AddressSelectId -> addressSelected)
  }
}