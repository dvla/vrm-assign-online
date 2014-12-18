package views.vrm_assign

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import composition.TestHarness
import org.openqa.selenium.WebDriver
import pages.vrm_assign.BeforeYouStartPage.startNow
import pages.vrm_assign.{VehicleLookupPage, BeforeYouStartPage}
import org.scalatest.selenium.WebBrowser._

final class BeforeYouStartIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      currentUrl should equal(BeforeYouStartPage.url)
    }

    "remove redundant cookies (needed for when a user exits the service and comes back)" taggedAs UiTag in new WebBrowserForSelenium {
      def cacheSetup()(implicit webDriver: WebDriver) =
        CookieFactoryForUISpecs.setupBusinessDetails().
          businessChooseYourAddress().
          enterAddressManually().
          businessDetails().
          vehicleAndKeeperDetailsModel()

      go to BeforeYouStartPage
      cacheSetup()
      go to BeforeYouStartPage

      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.AssignSet.foreach(cacheKey => webDriver.manage().getCookieNamed(cacheKey) should equal(null))
    }
  }

  "startNow button" should {

    "go to next page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      click on startNow

      currentUrl should equal(VehicleLookupPage.url)
    }
  }
}