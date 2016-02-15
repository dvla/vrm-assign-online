package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.currentUrl
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import pages.common.MainPanel.back
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.CaptureCertificateDetailsPage
import pages.vrm_assign.ConfirmPage
import pages.vrm_assign.LeaveFeedbackPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_assign.Confirm.ConfirmCacheKey

final class ConfirmIntegrationSpec extends UiSpec with TestHarness with Eventually with IntegrationPatience {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmPage
      currentUrl should equal(ConfirmPage.url)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to ConfirmPage
      val csrf: WebElement = webDriver.findElement(
        By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      )
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should
        equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").nonEmpty should equal(true)
    }

    "not display outstanding fees on page when no fees are due" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmPage
      pageSource shouldNot contain("Outstanding Renewal Fees")
      currentUrl should equal(ConfirmPage.url)
    }

    "display the page with blank keeper title" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs
        .vehicleAndKeeperLookupFormModel()
        .setupBusinessDetails()
        .vehicleAndKeeperDetailsModel(title = None)
        .captureCertificateDetailsFormModel()
        .captureCertificateDetailsModel()
        .businessDetails()
      go to ConfirmPage
      currentUrl should equal(ConfirmPage.url)
    }

    "display the page with blank keeper surname" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs
        .vehicleAndKeeperLookupFormModel()
        .setupBusinessDetails()
        .vehicleAndKeeperDetailsModel(lastName = None)
        .captureCertificateDetailsFormModel()
        .captureCertificateDetailsModel()
        .businessDetails()
      go to ConfirmPage
      currentUrl should equal(ConfirmPage.url)
    }

    "display the page with blank address" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetupWithEmptyAddress()
      go to ConfirmPage
      currentUrl should equal(ConfirmPage.url)
    }

  }

  "exit" should {
    "display feedback page when exit link is clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmPage
      click on ConfirmPage.exit
      currentUrl should equal(LeaveFeedbackPage.url)
    }

    "delete the Confirm cookie" taggedAs UiTag in
      new WebBrowserForSelenium(webDriver = WebDriverFactory.defaultBrowserPhantomJs) {
      go to BeforeYouStartPage
      cacheSetup().confirmFormModel()
      go to ConfirmPage
      click on ConfirmPage.exit
      webDriver.manage().getCookieNamed(ConfirmCacheKey) should equal(null)
    }
  }

  "back button" should {
    "redirect to SetUpBusinessDetails page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmPage
      click on back
      currentUrl should equal(CaptureCertificateDetailsPage.url)
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

  private def cacheSetupWithEmptyAddress()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel(emptyAddress = true).
      businessDetails().
      transactionId().
      captureCertificateDetailsModel().
      captureCertificateDetailsFormModel()
}