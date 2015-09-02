package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.currentUrl
import org.scalatest.selenium.WebBrowser.go
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.PaymentPreventBackPage
import pages.vrm_assign.PaymentPreventBackPage.returnToSuccess
import pages.vrm_assign.SuccessPage

final class PaymentPreventBackUiSpec extends UiSpec with TestHarness {

  "go to the page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to PaymentPreventBackPage

      currentUrl should equal(PaymentPreventBackPage.url)
    }
  }

  "returnToSuccess" should {
    "redirect to the PaymentSuccess page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to PaymentPreventBackPage
      click on returnToSuccess

      currentUrl should equal(SuccessPage.url)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      transactionId().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      fulfilModel().
      businessDetails().
      confirmFormModel().
      paymentTransNo().
      paymentModel().
      captureCertificateDetailsModel().
      captureCertificateDetailsFormModel()
}