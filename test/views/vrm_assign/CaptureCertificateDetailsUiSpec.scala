package views.vrm_assign

import composition.TestHarness
import helpers.vrm_assign.CookieFactoryForUISpecs
import pages.common.MainPanel.back
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.scalatest.selenium.WebBrowser.{go, currentUrl, click}
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.CaptureCertificateDetailsPage.date
import pages.vrm_assign.CaptureCertificateDetailsPage.documentCount
import pages.vrm_assign.CaptureCertificateDetailsPage.registrationMark
import pages.vrm_assign.CaptureCertificateDetailsPage.time
import pages.vrm_assign.CaptureCertificateDetailsPage
import pages.vrm_assign.ConfirmPage
import pages.vrm_assign.LeaveFeedbackPage
import pages.vrm_assign.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}
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
      val csrf: WebElement = webDriver.findElement(
        By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      )
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(
        uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName
      )
      csrf.getAttribute("value").nonEmpty should equal(true)
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

      click on CaptureCertificateDetailsPage.lookup

      currentUrl should equal(ConfirmPage.url)
    }
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
    "redirect to VehicleLookup page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to CaptureCertificateDetailsPage

      click on back

      currentUrl should equal(VehicleLookupPage.url)
    }

    "redirect to VehicleLookup page with ceg identifier" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup().withIdentifier("CEG")
      go to CaptureCertificateDetailsPage

      click on back

      currentUrl should equal(VehicleLookupPage.cegUrl)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleAndKeeperLookupFormModel(replacementVRN = "AB12AWR").
      vehicleAndKeeperDetailsModel().
      businessDetails().
      transactionId()
}
