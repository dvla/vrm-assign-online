package views.vrm_assign

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{By, WebDriver, WebElement}
import pages.common.MainPanel.back
import pages.vrm_assign._
import pages.vrm_assign.ConfirmPage._

final class ConfirmIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheSetup()

      go to ConfirmPage

      page.url should equal(ConfirmPage.url)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to VehicleLookupPage
      val csrf: WebElement = webDriver.findElement(By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }

    "not display outstanding fees on page when no fees are due" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheSetup()

      go to ConfirmPage

      page.source shouldNot contain("Outstanding Renewal Fees")
      //page.url should equal(ConfirmPage.url)
    }
  }

//  "confirm button" should {
//
//    "redirect to paymentPage when confirm link is clicked" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      cacheSetup()
//
//      happyPath
//
//      page.url should equal(SuccessPage.url)
//    }
//  }

  "exit" should {
    "display feedback page when exit link is clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheSetup()

      exitPath

      page.url should equal(LeaveFeedbackPage.url)
    }
  }

  "back button" should {

    "redirect to SetUpBusinessDetails page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmBusinessPage

      click on back

      page.url should equal(SetupBusinessDetailsPage.url)
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