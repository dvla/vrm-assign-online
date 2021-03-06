package views.vrm_assign

import composition.TestHarness
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_assign.LeaveFeedbackPage
import pages.vrm_assign.SuccessPage.finish
import pages.vrm_assign.{BeforeYouStartPage, SuccessPage}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}

class SuccessUiSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPage
      currentUrl should equal(SuccessPage.url)
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
        .confirmFormModel()
        .fulfilModel()
        .transactionId()
        .paymentTransNo()
        .paymentModel()
      go to SuccessPage
      currentUrl should equal(SuccessPage.url)
    }

    "display the page blank keeper surname" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs
        .vehicleAndKeeperLookupFormModel()
        .setupBusinessDetails()
        .vehicleAndKeeperDetailsModel(lastName = None)
        .captureCertificateDetailsFormModel()
        .captureCertificateDetailsModel()
        .businessDetails()
        .confirmFormModel()
        .fulfilModel()
        .transactionId()
        .paymentTransNo()
        .paymentModel()
      go to SuccessPage
      currentUrl should equal(SuccessPage.url)
    }
  }

  "finish" should {
    "redirect to feedback page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPage
      click on finish
      currentUrl should equal(LeaveFeedbackPage.url)
    }

    "remove redundant cookies (needed for when a user exits the service and comes back)" taggedAs UiTag in
      new WebBrowserForSelenium(webDriver = WebDriverFactory.defaultBrowserPhantomJsNoJs) {
        finishToSuccess()
    }

    "remove redundant cookies with ceg identifier" taggedAs UiTag in
      new WebBrowserForSelenium(webDriver = WebDriverFactory.defaultBrowserPhantomJsNoJs) {
      finishToSuccess(ceg = true)
    }
  }

  "print button" should {
    "have the label 'Print this page'" taggedAs UiTag in new WebBrowserWithJs {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPage
      SuccessPage.print.text should equal("Print this page")
    }
  }

  private def finishToSuccess(ceg: Boolean = false)(implicit webDriver: WebDriver) = {
      go to BeforeYouStartPage
      val cache = cacheSetup()
      if (ceg) cache.withIdentifier("ceg")
      go to SuccessPage
      click on finish
      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.AssignSet.foreach(cacheKey => webDriver.manage().getCookieNamed(cacheKey) should equal(null))
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .vehicleAndKeeperLookupFormModel()
      .setupBusinessDetails()
      .vehicleAndKeeperDetailsModel()
      .captureCertificateDetailsFormModel()
      .captureCertificateDetailsModel()
      .businessDetails()
      .confirmFormModel()
      .fulfilModel()
      .transactionId()
      .paymentTransNo()
      .paymentModel()
}
