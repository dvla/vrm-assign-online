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
import pages.vrm_assign.CaptureCertificateDetailsPage.registrationMark
import pages.vrm_assign.CaptureCertificateDetailsPage.time
import pages.vrm_assign.CaptureCertificateDetailsPage.prVrm
import pages.vrm_assign._

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

//  "confirm button" should {
//
//    "redirect to confirm page when next button is clicked" taggedAs UiTag in new WebBrowserForSelenium {
//      go to BeforeYouStartPage
//      cacheSetup()
//      go to CaptureCertificateDetailsPage
//      documentCount.value = "1"
//      date.value = "11111"
//      time.value = "111111"
//      registrationMark.value = "11111111"
//      prVrm.value = "A1"
//
//      click on CaptureCertificateDetailsPage.lookup
//
//      currentUrl should equal(ConfirmPage.url)
//    }
//  }

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