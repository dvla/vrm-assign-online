package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go, pageTitle, pageSource}
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.LeaveFeedbackPage
import pages.vrm_assign.VehicleLookupFailurePage
import pages.vrm_assign.VehicleLookupFailurePage.exit
import pages.vrm_assign.VehicleLookupFailurePage.tryAgain
import pages.vrm_assign.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final class VehicleLookupFailureIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the lookup unsuccessful page for a doc ref mismatch" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheDocRefMismatchSetup()
      go to VehicleLookupFailurePage

      pageTitle should equal(VehicleLookupFailurePage.title)
    }

    "display the lookup unsuccessful page for a direct to paper failure" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheDirectToPaperSetup()
      go to VehicleLookupFailurePage

      pageTitle should equal(VehicleLookupFailurePage.directToPaperTitle)
    }

    "display the lookup unsuccessful page for a post code mismatch" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cachePostcodeMismatchSetup()
      go to VehicleLookupFailurePage
      pageTitle should equal(VehicleLookupFailurePage.title)
    }


    "display the lookup unsuccessful page for a failure" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheFailureSetup()
      go to VehicleLookupFailurePage

      pageTitle should equal(VehicleLookupFailurePage.failureTitle)
    }
  }

  "contact details" should {
    def shouldDisplayContactInfo(cacheSetup: () => CookieFactoryForUISpecs.type)(implicit webDriver: WebDriver) = {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupFailurePage

      val element: WebElement = webDriver.findElement(By.className("contact-info-wrapper"))
      element.getAttribute("name") should equal("contact-info-wrapper")
      element should be ('displayed)
      element.getText.contains("Telephone") should equal(true)
    }

    "not contain contact information with a document reference mismatch" taggedAs UiTag in new WebBrowserForSelenium  {
      go to BeforeYouStartPage
      cacheDocRefMismatchSetup()
      go to VehicleLookupFailurePage

      intercept[org.openqa.selenium.NoSuchElementException] {
        val _: WebElement = webDriver.findElement(By.className("contact-info-wrapper"))
      }
    }

    "contain contact information with a eligibility failure" taggedAs UiTag in new WebBrowserForSelenium  {
      shouldDisplayContactInfo(cacheFailureSetup)
    }

    "contain contact information with a direct to paper failure" taggedAs UiTag in new WebBrowserForSelenium  {
      shouldDisplayContactInfo(cacheDirectToPaperSetup)
    }
  }

  "page should not contain vehicle details" should {
    "not contain the vehicle make or model" taggedAs UiTag in new WebBrowserForSelenium  {
      go to BeforeYouStartPage
      cacheFailureSetup()
      go to VehicleLookupFailurePage
      pageSource should not include "Vehicle make"
      pageSource should not include "Vehicle model"
    }
  }

  "registration numbers" should {
    "be correctly formatted" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheFailureSetup()
      go to VehicleLookupFailurePage

      val regNumbers = webDriver.findElements(By.className("reg-number")).iterator()
      val displayedReg = regNumbers.next.getText
      val displayedVRN = regNumbers.next.getText

      // Trim the result of formatVrm because Selenium trims any WebElement text.
      displayedReg should be (formatVrm(displayedReg.filter(p => !p.isSpaceChar)).trim)
      displayedVRN should be (formatVrm(displayedVRN.filter(p => !p.isSpaceChar)).trim)
    }
  }

  "try again button" should {
    "redirect to vehicle lookup page when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheDocRefMismatchSetup()

      go to VehicleLookupFailurePage

      click on tryAgain

      currentUrl should equal(VehicleLookupPage.url)
    }
  }

  "exit button" should {
    "redirect to feedback page when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheDocRefMismatchSetup()

      go to VehicleLookupFailurePage

      click on exit

      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }

  private def cacheDocRefMismatchSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      transactionId().
      bruteForcePreventionViewModel().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      storeMsResponseCode(message = "vehicle_and_keeper_lookup_document_reference_mismatch")

  private def cacheDirectToPaperSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      transactionId().
      bruteForcePreventionViewModel().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      storeMsResponseCode(message = "vrm_assign_eligibility_direct_to_paper")

  private def cacheFailureSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      transactionId().
      bruteForcePreventionViewModel().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      storeMsResponseCode(message = "vrm_assign_eligibility_failure")

  private def cachePostcodeMismatchSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .transactionId()
      .bruteForcePreventionViewModel()
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
      .storeMsResponseCode(message = controllers.VehicleLookup.RESPONSE_CODE_POSTCODE_MISMATCH)

}
