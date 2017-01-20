package views.vrm_assign

import composition.TestHarness
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{go, click, currentUrl}
import pages.common.MainPanel.back
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.CaptureCertificateDetailsPage
import pages.vrm_assign.ConfirmBusinessPage
import pages.vrm_assign.ConfirmBusinessPage.confirm
import pages.vrm_assign.ConfirmBusinessPage.exit
import pages.vrm_assign.LeaveFeedbackPage
import pages.vrm_assign.SetupBusinessDetailsPage
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}

class ConfirmBusinessIntegrationSpec extends UiSpec with TestHarness {

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

  "back button" should {
    "redirect to SetupBusinessDetails page when we navigate back" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup().setupBusinessDetails()
      go to ConfirmBusinessPage
      click on back
      currentUrl should equal(SetupBusinessDetailsPage.url)
    }

    "redirect to SetUpBusinessDetails page when we navigate backwards" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmBusinessPage
      click on back
      currentUrl should equal(SetupBusinessDetailsPage.url)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
      .businessDetails()
      .transactionId()
      .setupBusinessDetails()
}
