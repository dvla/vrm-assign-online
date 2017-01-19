package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{currentUrl, click, go}
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.ErrorPage
import pages.vrm_assign.ErrorPage.startAgain
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import uk.gov.dvla.vehicles.presentation.common.testhelpers.UiTag

class ErrorUiSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to ErrorPage
      currentUrl should equal(ErrorPage.url)
    }
  }

  "startAgain button" should {
    "remove redundant cookies (needed for when a user exits the service and comes back)" taggedAs UiTag in
      new WebBrowserForSelenium(webDriver = WebDriverFactory.defaultBrowserPhantomJs){
      def cacheSetup()(implicit webDriver: WebDriver) =
        CookieFactoryForUISpecs.setupBusinessDetails()
          .businessDetails()
          .vehicleAndKeeperDetailsModel()

      go to BeforeYouStartPage
      cacheSetup()
      go to ErrorPage
      click on startAgain

      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.AssignSet.foreach(cacheKey => webDriver.manage().getCookieNamed(cacheKey) should equal(null))
    }
  }
}
