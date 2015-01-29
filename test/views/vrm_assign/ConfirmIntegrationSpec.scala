package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.selenium.WebBrowser._
import pages.common.MainPanel.back
import pages.vrm_assign.ConfirmPage._
import pages.vrm_assign._

final class ConfirmIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheSetup()

      go to ConfirmPage

      currentUrl should equal(ConfirmPage.url)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to VehicleLookupPage
      val csrf: WebElement = webDriver.findElement(By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }

    "not display outstanding fees on page when no fees are due" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheSetup()

      go to ConfirmPage

      pageSource shouldNot contain("Outstanding Renewal Fees")
      currentUrl should equal(ConfirmPage.url)
    }
  }

  //  "confirm button" should {
  //
  //    "redirect to paymentPage when confirm link is clicked" taggedAs UiTag in new WebBrowserForSelenium {
  //      go to BeforeYouStartPage
  //
  //      cacheSetup()
  //
  //      happyPath
  //
  //  currentUrl should equal(SuccessPage.url)
  //    }
  //  }

  "exit" should {
    "display feedback page when exit link is clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheSetup()

      exitPath

      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }

  "back button" should {

    "redirect to SetUpBusinessDetails page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmBusinessPage

      click on back

      currentUrl should equal(SetupBusinessDetailsPage.url)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      businessDetails().
      transactionId().
      captureCertificateDetailsModel().
      captureCertificateDetailsFormModel()
}