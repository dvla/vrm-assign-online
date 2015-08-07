package views.vrm_assign

import composition.TestHarness
import controllers.routes.CookiePolicy
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go, pageTitle, pageSource}
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.BeforeYouStartPage.footerItem
import pages.vrm_assign.BeforeYouStartPage.startNow
import pages.vrm_assign.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import uk.gov.dvla.vehicles.presentation.common.controllers.AlternateLanguages.CyId
import uk.gov.dvla.vehicles.presentation.common.controllers.routes.AlternateLanguages

class BeforeYouStartIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      currentUrl should equal(BeforeYouStartPage.url)
    }

    "remove redundant cookies (needed for when a user exits the service and comes back)" taggedAs UiTag in
      new WebBrowserForSelenium(webDriver = WebDriverFactory.defaultBrowserPhantomJs) {
      def cacheSetup()(implicit webDriver: WebDriver) =
        CookieFactoryForUISpecs.vehicleAndKeeperDetailsModel()
          .setupBusinessDetails()
//          .businessChooseYourAddress()
//          .enterAddressManually()
          .businessDetails()

      go to BeforeYouStartPage
      cacheSetup()
      go to BeforeYouStartPage

      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.AssignSet.foreach(cacheKey => webDriver.manage().getCookieNamed(cacheKey) should equal(null))
    }

    "display the global cookie message when cookie 'seen_cookie_message' does not exist" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      pageSource should include("Find out more about cookies")
    }

    "display a link to the cookie policy" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      footerItem(index = 0).findElement(By.tagName("a")).getAttribute("href") should include(CookiePolicy.present().toString())
    }

    "display a Cymraeg link" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      footerItem(index = 1).findElement(By.tagName("a")).getAttribute("href") should include(AlternateLanguages.withLanguage(CyId).toString())
    }

    "change language to Welsh when Cymraeg link clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      click on footerItem(index = 1).findElement(By.tagName("a"))
      pageTitle should equal(BeforeYouStartPage.titleCy)
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
