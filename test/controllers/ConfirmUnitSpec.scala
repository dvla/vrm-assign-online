package controllers

import helpers.UnitSpec
import helpers.vrm_assign.CookieFactoryForUnitSpecs.captureCertificateDetailsFormModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.captureCertificateDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import helpers.WithApplication
import pages.vrm_assign.FulfilPage
import play.api.test.FakeRequest
import play.api.test.Helpers.BAD_REQUEST
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.OK
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.{EmailId, EmailVerifyId}
import uk.gov.dvla.vehicles.presentation.common.testhelpers.CookieHelper.fetchCookiesFromHeaders
import views.vrm_assign.Confirm.ConfirmCacheKey
import views.vrm_assign.Confirm.GranteeConsentId
import views.vrm_assign.Confirm.KeeperEmailId
import views.vrm_assign.Confirm.SupplyEmail_true
import views.vrm_assign.Confirm.SupplyEmailId
import views.vrm_assign.VehicleLookup.UserType_Keeper
import webserviceclients.fakes.ConfirmFormConstants.KeeperEmailValid

class ConfirmUnitSpec extends UnitSpec {

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
        cookies.map(_.name) should contain(ConfirmCacheKey)
      }
    }

    "return a bad request when the supply email field has nothing selected" in new WithApplication {
      val request = buildRequest(supplyEmail = supplyEmailEmpty)
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Keeper),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel()
        )

      val result = confirm.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "return a bad request when the keeper wants to supply an email " +
      "and does not provide an email address" in new WithApplication {
      val request = buildRequest(keeperEmail = keeperEmailEmpty)
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Keeper),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel()
        )

      val result = confirm.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }
  }

  private def confirm = testInjector().getInstance(classOf[Confirm])

  private def present = {
    val request = FakeRequest()
      .withCookies(vehicleAndKeeperDetailsModel())
      .withCookies(vehicleAndKeeperLookupFormModel())
      .withCookies(captureCertificateDetailsFormModel())
      .withCookies(captureCertificateDetailsModel())
    confirm.present(request)
  }

  private def submit = {
    val request = buildRequest()
      .withCookies(vehicleAndKeeperDetailsModel())
      .withCookies(vehicleAndKeeperLookupFormModel())
      .withCookies(captureCertificateDetailsFormModel())
      .withCookies(captureCertificateDetailsModel())
    confirm.submit(request)
  }

  private val supplyEmailEmpty = ""
  private val keeperEmailEmpty = ""

  private def buildRequest(keeperEmail: String = KeeperEmailValid, supplyEmail: String = SupplyEmail_true) = {
    FakeRequest().withFormUrlEncodedBody(
      s"$KeeperEmailId.$EmailId" -> keeperEmail,
      s"$KeeperEmailId.$EmailVerifyId" -> keeperEmail,
      GranteeConsentId -> "true",
      SupplyEmailId -> supplyEmail
    )
  }
}
