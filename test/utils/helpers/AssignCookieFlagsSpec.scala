package utils.helpers

import helpers.UnitSpec
import play.api.mvc.Cookie
import play.api.test.WithApplication
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieFlags
import views.vrm_assign.ConfirmBusiness.StoreBusinessDetailsCacheKey

import scala.concurrent.duration.DurationInt

final class AssignCookieFlagsSpec extends UnitSpec {

  "applyToCookie (no key passed in)" should {
    "return cookie with max age and secure flag" in new WithApplication {
      val originalCookie = Cookie(name = "testCookieName", value = "testCookieValue")

      originalCookie.secure should equal(false)
      originalCookie.maxAge should equal(None)

      // This will load values from the fake config we are passing into this test's WithApplication.
      val modifiedCookie = cookieFlags.applyToCookie(originalCookie)
      modifiedCookie.secure should equal(false)
      modifiedCookie.maxAge should equal(Some(30.minutes.toSeconds.toInt))
    }

    "return cookie with max age, secure flag and domain when key " +
      "is for a BusinessDetails cookie" in new WithApplication {
      val originalCookie = Cookie(name = StoreBusinessDetailsCacheKey, value = "testCookieValue")

      originalCookie.secure should equal(false)
      originalCookie.maxAge should equal(None)

      // This will load values from the fake config we are passing into this test's WithApplication.
      val modifiedCookie = cookieFlags.applyToCookie(originalCookie, StoreBusinessDetailsCacheKey)
      modifiedCookie.secure should equal(false)
      modifiedCookie.maxAge should equal(Some(7.days.toSeconds.toInt))
    }
  }

  private def cookieFlags = testInjector().getInstance(classOf[CookieFlags])
}