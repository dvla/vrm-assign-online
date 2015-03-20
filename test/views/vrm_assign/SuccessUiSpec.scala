package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.scalatest.selenium.WebBrowser._
import pages.common.ErrorPanel
import pages.vrm_assign.VehicleLookupPage.fillWith
import pages.vrm_assign._

final class SuccessUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()

      go to SuccessPage

      currentUrl should equal(SuccessPage.url)
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
      businessChooseYourAddress().
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