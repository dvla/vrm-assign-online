package views.vrm_assign

import composition.TestHarness
import helpers.tags.UiTag
import helpers.UiSpec
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_assign.PaymentFailurePage.exit
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.LeaveFeedbackPage
import pages.vrm_assign.FulfilFailurePage

class FulfilFailureIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the Fulfil failure page for an invalid retain request" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheInvalidRetainRequestSetup()
      go to FulfilFailurePage
      currentUrl should equal(FulfilFailurePage.url)
    }
  }

  "exit button" should {
    "redirect to feedback page when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheInvalidRetainRequestSetup()
      go to FulfilFailurePage
      click on exit
      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }

  private def cacheInvalidRetainRequestSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .transactionId()
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
      .captureCertificateDetailsModel()
      .captureCertificateDetailsFormModel()
      .paymentModel()
}
