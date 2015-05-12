package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.ConfirmBusinessPage.confirm
import pages.vrm_assign.ConfirmBusinessPage.exit
import pages.vrm_assign._

final class ConfirmBusinessIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()

      go to ConfirmBusinessPage

      currentUrl should equal(ConfirmBusinessPage.url)
    }
  }

  "confirm button" should {

    "redirect to Confirm business page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmBusinessPage

      click on confirm

      currentUrl should equal(CaptureCertificateDetailsPage.url)
    }
  }

  "exit button" should {

    "display feedback page when exit link is clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmBusinessPage

      click on exit

      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }


  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      businessDetails().
      transactionId().
      setupBusinessDetails()
}