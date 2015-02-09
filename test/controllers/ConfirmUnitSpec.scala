package controllers

import composition.WithApplication
import helpers.UnitSpec
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_assign.CookieFactoryForUnitSpecs.{captureCertificateDetailsFormModel, captureCertificateDetailsModel, vehicleAndKeeperDetailsModel, vehicleAndKeeperLookupFormModel}
import pages.vrm_assign.FulfilPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.vrm_assign.Confirm._
import webserviceclients.fakes.ConfirmFormConstants.KeeperEmailValid

final class ConfirmUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }
  }

  "submit" should {

    "redirect to next page when the form is completed successfully" in new WithApplication {
      whenReady(submit) { r =>
        r.header.headers.get(LOCATION) should equal(Some(FulfilPage.address))
      }
    }

    "write cookies to the cache when a valid form is submitted" in new WithApplication {
      whenReady(submit) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(KeeperEmailCacheKey)
      }
    }
  }

  private def confirm = testInjector().getInstance(classOf[Confirm])

  private def present = {
    val request = FakeRequest().
      withCookies(vehicleAndKeeperDetailsModel()).
      withCookies(vehicleAndKeeperLookupFormModel()).
      withCookies(captureCertificateDetailsFormModel()).
      withCookies(captureCertificateDetailsModel())
    confirm.present(request)
  }

  private def submit = {
    val request = buildCorrectlyPopulatedRequest().
      withCookies(vehicleAndKeeperDetailsModel()).
      withCookies(vehicleAndKeeperLookupFormModel()).
      withCookies(captureCertificateDetailsFormModel()).
      withCookies(captureCertificateDetailsModel())
    confirm.submit(request)
  }

  private val supplyEmailTrue = "true"

  private def buildCorrectlyPopulatedRequest(keeperEmail: String = KeeperEmailValid, supplyEmail: String = supplyEmailTrue) = {
    FakeRequest().withFormUrlEncodedBody(
      KeeperEmailId -> keeperEmail,
      GranteeConsentId -> "true",
      SupplyEmailId -> supplyEmail
    )
  }
}