package views.vrm_assign

import helpers.vrm_assign.CookieFactoryForUISpecs
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.{PaymentNotAuthorisedPage, BeforeYouStartPage, LeaveFeedbackPage}
import helpers.UiSpec
import helpers.tags.UiTag
import composition.TestHarness
import org.openqa.selenium.WebDriver
import pages.vrm_assign.PaymentNotAuthorisedPage.{exit, tryAgain}


final class PaymentNotAuthorisedIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the payment not authorised page for a not authorised payment response" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheNotAuthorisedSetup()

      go to PaymentNotAuthorisedPage

      currentUrl should equal(PaymentNotAuthorisedPage.url)
    }
  }

  // TODO restore when payment iframe is back
//  "try again button" should {
//    "redirect to success page when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
//      go to BeforeYouStartPage
//
//      cacheNotAuthorisedSetup()
//
//      go to PaymentNotAuthorisedPage
//
//      click on tryAgain
//
//    currentUrlrl should equal(PaymentPage.url)
//    }
//  }

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
      transactionId().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      captureCertificateDetailsModel().
      captureCertificateDetailsFormModel()
}