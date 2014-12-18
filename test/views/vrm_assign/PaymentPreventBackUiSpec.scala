package views.vrm_assign

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import composition.TestHarness
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.{SuccessPage, BeforeYouStartPage, PaymentPreventBackPage}
import pages.vrm_assign.PaymentPreventBackPage.returnToSuccess

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
      keeperEmail().
      paymentTransNo().
      paymentModel().
      captureCertificateDetailsModel().
      captureCertificateDetailsFormModel()
}