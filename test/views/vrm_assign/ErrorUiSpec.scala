package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.ErrorPage
import pages.vrm_assign.ErrorPage.startAgain

final class ErrorUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to ErrorPage

      currentUrl should equal(ErrorPage.url)
    }
  }

  "startAgain button" should {

    "remove redundant cookies (needed for when a user exits the service and comes back)" taggedAs UiTag in new WebBrowserForSelenium {
      def cacheSetup()(implicit webDriver: WebDriver) =
        CookieFactoryForUISpecs.setupBusinessDetails().
          businessChooseYourAddress().
          enterAddressManually().
          businessDetails().
          vehicleAndKeeperDetailsModel()

      go to BeforeYouStartPage
      cacheSetup()
      go to ErrorPage
      click on startAgain

      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.AssignSet.foreach(cacheKey => webDriver.manage().getCookieNamed(cacheKey) should equal(null))
    }
  }
}