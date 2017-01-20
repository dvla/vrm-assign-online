package views.vrm_assign

import composition.TestHarness
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.{By, WebElement, WebDriver}
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.currentUrl
import org.scalatest.selenium.WebBrowser.go
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.LeaveFeedbackPage
import pages.vrm_assign.PaymentNotAuthorisedPage
import pages.vrm_assign.PaymentNotAuthorisedPage.exit
import pages.vrm_assign.PaymentNotAuthorisedPage.tryAgain
import pages.vrm_assign.PaymentPage
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}
import webserviceclients.fakes.CaptureCertificateDetailsWebServiceConstants.ExpiredWithFeeCertificate

final class PaymentNotAuthorisedIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the payment not authorised page for a " +
      "not authorised payment response" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheNotAuthorisedSetup()

      go to PaymentNotAuthorisedPage

      currentUrl should equal(PaymentNotAuthorisedPage.url)
    }

    "contain the vehicle make and/or model" taggedAs UiTag in  new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheNotAuthorisedSetup()
      go to PaymentNotAuthorisedPage
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
      cacheNotAuthorisedSetup()
      go to PaymentNotAuthorisedPage
      val element: WebElement = webDriver.findElement(
        By.className("contact-info-wrapper")
      )
      element.getAttribute("name") should equal("contact-info-wrapper")
      element.isDisplayed() should equal(true)
      element.getText().contains("Telephone") should equal (true)
    }
  }

  "try again button" should {
    "redirect to success page when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheNotAuthorisedSetup()

      go to PaymentNotAuthorisedPage

      click on tryAgain

      currentUrl should equal(PaymentPage.url)
    }
  }

  "exit button" should {
    "redirect to feedback page when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheNotAuthorisedSetup()

      go to PaymentNotAuthorisedPage

      click on exit

      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }

  private def cacheNotAuthorisedSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      businessDetails().
      confirmFormModel().
      granteeConsent().
      transactionId().
      paymentModel().
      paymentTransNo().
      captureCertificateDetailsModel(certificate = ExpiredWithFeeCertificate).
      captureCertificateDetailsFormModel()
}
