package views.vrm_assign

import composition.TestHarness
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_assign.{BeforeYouStartPage, LeaveFeedbackPage, PaymentPage, VehicleLookupPage}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}
import views.vrm_assign.RelatedCacheKeys.AssignSet
import views.vrm_assign.RelatedCacheKeys.BusinessDetailsSet
import webserviceclients.fakes.CaptureCertificateDetailsWebServiceConstants.ExpiredWithFeeCertificate

class PaymentIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to PaymentPage
      currentUrl should equal(PaymentPage.url)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to VehicleLookupPage
      val csrf: WebElement = webDriver.findElement(
        By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      )
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(
        uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName
      )
      csrf.getAttribute("value").length > 0 should equal(true)
    }

    "redirect to VehicleLookupPage page when fulfil cookie is present " +
      "(the user has manually changed the url to get here)" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup().fulfilModel()
      go to PaymentPage
      currentUrl should equal(VehicleLookupPage.url)
    }
  }

  // Cannot test without mocking up the html of the Solve payment iframe
  // "pay now button" should

  "cancel" should {
    "redirect to mock feedback page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to PaymentPage
      click on PaymentPage.cancel
      currentUrl should equal(LeaveFeedbackPage.url)
    }

    "remove AssignSet cookies when storeBusinessDetailsConsent cookie does not exist" taggedAs UiTag in
      new WebBrowserForSelenium(webDriver = WebDriverFactory.defaultBrowserPhantomJs) {
      go to BeforeYouStartPage
      cacheSetup()
      go to PaymentPage
      click on PaymentPage.cancel
      // Verify the cookies identified by the full set of cache keys have been removed
      AssignSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }

    "remove AssignSet and BusinessDetailsSet cookies when storeBusinessDetailsConsent cookie is false" taggedAs UiTag in
      new WebBrowserForSelenium(webDriver = WebDriverFactory.defaultBrowserPhantomJs) {
      go to BeforeYouStartPage
      cacheSetup()
        .setupBusinessDetails()
        .storeBusinessDetailsConsent(consent = "false")
      go to PaymentPage

      click on PaymentPage.cancel

      // Verify the cookies identified by the full set of cache keys have been removed
      BusinessDetailsSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })

      AssignSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }

    "remove AssignSet cookies when storeBusinessDetailsConsent cookie contains true" taggedAs UiTag in
      new WebBrowserForSelenium(webDriver = WebDriverFactory.defaultBrowserPhantomJs) {
      go to BeforeYouStartPage
      cacheSetup()
        .setupBusinessDetails()
        .storeBusinessDetailsConsent(consent = "true")
      go to PaymentPage

      click on PaymentPage.cancel

      // Verify the cookies identified by the full set of cache keys have been removed
      BusinessDetailsSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should not equal null // Verify not removed in this case!
      })

      AssignSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      businessDetails().
      confirmFormModel().
      granteeConsent().
      transactionId().
      paymentModel().
      paymentTransNo().
      captureCertificateDetailsModel(certificate = ExpiredWithFeeCertificate).
      captureCertificateDetailsFormModel()
}
