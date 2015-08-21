package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_assign.LeaveFeedbackPage
import pages.vrm_assign.SuccessPage.finish
import pages.vrm_assign.{BeforeYouStartPage, SuccessPage}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory

class SuccessUiSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPage
      currentUrl should equal(SuccessPage.url)
    }
  }

  "finish" should {
    "redirect to feedback page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPage
      click on finish
      currentUrl should equal(LeaveFeedbackPage.url)
    }

    "remove redundant cookies (needed for when a user exits the service and comes back)" taggedAs UiTag in
      new WebBrowserForSelenium(webDriver = WebDriverFactory.defaultBrowserPhantomJs) {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPage
      click on finish

      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.AssignSet.foreach(cacheKey => webDriver.manage().getCookieNamed(cacheKey) should equal(null))
    }
  }

  "print button" should {
    "have the label 'Print this page'" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPage
      SuccessPage.print.text should equal("Print this page")
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleAndKeeperLookupFormModel().
      setupBusinessDetails().
      vehicleAndKeeperDetailsModel().
      captureCertificateDetailsFormModel().
      captureCertificateDetailsModel().
      businessDetails().
      confirmFormModel().
      fulfilModel().
      transactionId().
      paymentTransNo().
      paymentModel()
}