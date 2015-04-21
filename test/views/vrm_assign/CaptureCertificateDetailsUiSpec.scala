package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.scalatest.selenium.WebBrowser._
import pages.common.MainPanel.back
import pages.vrm_assign.CaptureCertificateDetailsPage.date
import pages.vrm_assign.CaptureCertificateDetailsPage.documentCount
import pages.vrm_assign.CaptureCertificateDetailsPage.prVrm
import pages.vrm_assign.CaptureCertificateDetailsPage.registrationMark
import pages.vrm_assign.CaptureCertificateDetailsPage.time
import pages.vrm_assign._
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid

final class CaptureCertificateDetailsUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheSetup()

      go to CaptureCertificateDetailsPage

      currentUrl should equal(CaptureCertificateDetailsPage.url)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to CaptureCertificateDetailsPage
      val csrf: WebElement = webDriver.findElement(By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "lookup button" should {

    "redirect to confirm page when next button is clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to CaptureCertificateDetailsPage
      documentCount.value = "1"
      date.value = "11111"
      time.value = "111111"
      registrationMark.value = RegistrationNumberValid
      prVrm.value = RegistrationNumberValid

      click on CaptureCertificateDetailsPage.lookup

      currentUrl should equal(ConfirmPage.url)
    }
  }

  "certificate field" should {
//    "auto tab when entering the maximum number of characters" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
//      go to BeforeYouStartPage
//      cacheSetup()
//      go to CaptureCertificateDetailsPage
//
//      documentCount.underlying.sendKeys("1", "11111", "111111", "11111111") // using sendKeys so that when the text of
//      // the max length for that field is entered it should tab to the next field.
//
//      documentCount.value should equal("1")
//      date.value should equal("11111")
//      time.value should equal("111111")
//      registrationMark.value should equal("11111111")
//    }
//
//    "allow the user to change the value they have already entered in a field when they go back to the field (it should not immediatly auto-tab them away on arrival into the field)" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
//      go to BeforeYouStartPage
//      cacheSetup()
//      go to CaptureCertificateDetailsPage
//      documentCount.underlying.sendKeys("1", "11111", "111111", "11111111") // using sendKeys so that when the text of
//      // the max length for that field is entered it should tab to the next field.
//      // Verify that all the fields have the values we would expect if auto-tabbing works
//      documentCount.value should equal("1")
//      date.value should equal("11111")
//      time.value should equal("111111")
//      registrationMark.value should equal("11111111")
//
//      // Starting from the first field, delete some values and write new values. When the number of characters is back
//      // up to the max length it should auto-yab to the next field, but NOT before!
//      documentCount.underlying.sendKeys(
//        Keys.BACK_SPACE, "2", // edit documentCount
//        Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, "222", // edit date
//        Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, "222", // edit time
//        Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, "222" // edit registrationMark
//      )
//
//      documentCount.value should equal("2")
//      date.value should equal("11222")
//      time.value should equal("111222")
//      registrationMark.value should equal("11111222")
//    }
  }

  "exit" should {
    "display feedback page when exit link is clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to CaptureCertificateDetailsPage

      click on CaptureCertificateDetailsPage.exit

      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }

  "back button" should {

    "redirect to SetUpBusinessDetails page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to CaptureCertificateDetailsPage

      click on back

      currentUrl should equal(VehicleLookupPage.url)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      businessDetails().
      transactionId()
}