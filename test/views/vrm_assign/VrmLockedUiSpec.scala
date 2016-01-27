package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.LeaveFeedbackPage
import pages.vrm_assign.VrmLockedPage
import pages.vrm_assign.VrmLockedPage.exit
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory


final class VrmLockedUiSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup
      go to VrmLockedPage

      currentUrl should equal(VrmLockedPage.url)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to VrmLockedPage
      val csrf: WebElement = webDriver.findElement(
        By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      )
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should
        equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").nonEmpty should equal(true)
    }

    "contain contact information" taggedAs UiTag in  new WebBrowserForSelenium  {
      go to BeforeYouStartPage
      cacheSetup
      go to VrmLockedPage
      val element: WebElement = webDriver.findElement(
        By.className("contact-info-wrapper")
      )
      element.getAttribute("name") should equal("contact-info-wrapper")
      element.isDisplayed() should equal(true)
      element.getText().contains("Telephone") should equal (true)
    }

    "not contain the vehicle make or model" taggedAs UiTag in  new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup
      go to VrmLockedPage
      val element: WebElement = webDriver.findElement(
        By.className("playback")
      )
      element.getAttribute("class") should equal("playback")
      element.isDisplayed() should equal(true)
      element.getText().contains("Vehicle make") should equal (false)
      element.getText().contains("Vehicle model") should equal (false)
    }

    "contain the time of locking" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup
      go to VrmLockedPage
      val localTime: WebElement = webDriver.findElement(By.id("localTimeOfVrmLock"))
      localTime.isDisplayed should equal(true)
      localTime.getText.contains("UTC") should equal (true)
    }

    "contain the time of locking when JavaScript is disabled" taggedAs UiTag in new WebBrowserWithJsDisabled {
      go to BeforeYouStartPage
      cacheSetup
      go to VrmLockedPage
      val localTime: WebElement = webDriver.findElement(By.id("localTimeOfVrmLock"))
      localTime.isDisplayed should equal(true)
      localTime.getText.contains("UTC") should equal (true)
    }
  }

  "exit button" should {

    "redirect to feedback page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to VrmLockedPage

      click on exit

      currentUrl should equal(LeaveFeedbackPage.url)
    }

    "remove redundant cookies" taggedAs UiTag in
      new WebBrowserForSelenium(webDriver = WebDriverFactory.defaultBrowserPhantomJs) {
      go to BeforeYouStartPage
      cacheSetup()
      go to VrmLockedPage

      click on exit

      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.AssignSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }
  }


  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      transactionId().
      bruteForcePreventionViewModel().
      vehicleAndKeeperDetailsModel().
      vehicleAndKeeperLookupFormModel()


}
