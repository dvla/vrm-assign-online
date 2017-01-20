package views.vrm_assign

import composition.TestHarness
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.{By, WebElement, WebDriver}
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.LeaveFeedbackPage
import pages.vrm_assign.PaymentFailurePage
import pages.vrm_assign.PaymentFailurePage.exit
import pages.vrm_assign.PaymentFailurePage.tryAgain
import pages.vrm_assign.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}

final class PaymentFailureIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the payment failure page for an invalid " +
      "begin web payment request" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheInvalidBeginRequestSetup()

      go to PaymentFailurePage

      currentUrl should equal(PaymentFailurePage.url)
    }

    "contain the vehicle make and/or model" taggedAs UiTag in  new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheInvalidBeginRequestSetup()
      go to PaymentFailurePage
      val element: WebElement = webDriver.findElement(
        By.className("playback")
      )
      element.getAttribute("class") should equal("playback")
      element.isDisplayed() should equal(true)
      element.getText().contains("Vehicle make") should equal (true)
      element.getText().contains("Vehicle model") should equal (true)
    }

    "contain contact information" taggedAs UiTag in  new WebBrowserForSelenium  {
      go to BeforeYouStartPage
      cacheInvalidBeginRequestSetup()
      go to PaymentFailurePage
      val element: WebElement = webDriver.findElement(
        By.className("contact-info-wrapper")
      )
      element.getAttribute("name") should equal("contact-info-wrapper")
      element.isDisplayed() should equal(true)
      element.getText().contains("Telephone") should equal (true)
    }
  }

  "try again button" should {
    "redirect to confirm page when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheInvalidBeginRequestSetup()

      go to PaymentFailurePage

      click on tryAgain

      currentUrl should equal(VehicleLookupPage.url)
    }
  }

  "exit button" should {
    "redirect to feedback page when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheInvalidBeginRequestSetup()

      go to PaymentFailurePage

      click on exit

      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }

  private def cacheInvalidBeginRequestSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      transactionId().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      captureCertificateDetailsModel().
      captureCertificateDetailsFormModel()
}
