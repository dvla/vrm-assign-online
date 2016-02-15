package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.selenium.WebBrowser.{currentUrl, go, pageTitle}
import pages.common.ErrorPanel
import pages.vrm_assign.VehicleLookupPage.fillWith
import pages.vrm_assign.{ErrorPage, BeforeYouStartPage, CaptureCertificateDetailsPage, ConfirmBusinessPage}
import pages.vrm_assign.{SetupBusinessDetailsPage, VehicleLookupFailurePage, VehicleLookupPage}
import uk.gov.dvla.vehicles.presentation.common.views.widgetdriver.Wait

final class VehicleLookupIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      go to VehicleLookupPage

      currentUrl should equal(VehicleLookupPage.url)
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
      csrf.getAttribute("value").nonEmpty should equal(true)
    }

    "display the v5c image on the page with Javascript disabled" taggedAs UiTag in new WebBrowserForSelenium {
      go to VehicleLookupPage
      Wait.until(ExpectedConditions.visibilityOfElementLocated(
        By.xpath("//div[@data-tooltip='tooltip_document-reference-number']")),
        5
      )
    }

    "put the v5c image in a tooltip with Javascript enabled" taggedAs UiTag in
      new WebBrowserWithJs {
      go to VehicleLookupPage
      val v5c = By.xpath("//div[@data-tooltip='tooltip_document-reference-number']")
      Wait.until(ExpectedConditions.presenceOfElementLocated(v5c), 5)
      Wait.until(ExpectedConditions.invisibilityOfElementLocated(v5c), 5)
    }
  }

  "findVehicleDetails button" should {
    "display one validation error message when " +
      "no referenceNumber is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      fillWith(referenceNumber = "")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when " +
      "no registrationNumber is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      fillWith(registrationNumber = "")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when " +
      "a registrationNumber is entered containing one character" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      fillWith(registrationNumber = "a")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when " +
      "a registrationNumber is entered containing special characters" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      fillWith(registrationNumber = "$^")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display two validation error messages when " +
      "no vehicle details are entered but consent is given" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      fillWith(referenceNumber = "", registrationNumber = "")

      ErrorPanel.numberOfErrors should equal(2)
    }

    "display one validation error message when only " +
      "a valid referenceNumber is entered and consent is given" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      fillWith(registrationNumber = "")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when " +
      "an invalid postcode is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      fillWith(postcode = "!@X")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when " +
      "only a valid registrationNumber is entered and consent is given" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      fillWith(replacementVRN = "",
              referenceNumber = "",
              postcode = "") // Note : postcode is effectively optional

      ErrorPanel.numberOfErrors should equal(2)
    }

//    "display the capture certificate page for a unhandled business exception failure" taggedAs UiTag in new WebBrowserForSelenium {
//      go to BeforeYouStartPage
//      cachePreLookupSetup()
//      fillWith(registrationNumber = "H1") // business exception (GetVehicleAndKeeperDetailsInvalidDataException)
//      pageTitle should equal(CaptureCertificateDetailsPage.title) // UserType = keeper (purchaser)
//    }

    "display the lookup failure page (vrm not found) for a non-unhandled business exception failure" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cachePreLookupSetup()
//      fillWith(registrationNumber = "H1") // technical exception (GetVehicleAndKeeperDetailsTechnicalException)
      fillWith(registrationNumber = "VNF1") // technical exception (GetVehicleAndKeeperDetailsTechnicalException)
      currentUrl should equal(VehicleLookupFailurePage.url)
      pageTitle should equal(VehicleLookupFailurePage.title)
    }

    "display the setup business page for a non-unhandled exception failure and business user without details" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cachePreLookupSetup()
      fillWith(registrationNumber = "I1", isCurrentKeeper = false)
      pageTitle should equal(ErrorPage.title)
    }

//    "display the setup business page for a non-unhandled exception failure and business user without details" taggedAs UiTag in new WebBrowserForSelenium {
//      go to BeforeYouStartPage
//      cachePreLookupSetup()
//      fillWith(registrationNumber = "I1", isCurrentKeeper = false)
//      pageTitle should equal(SetupBusinessDetailsPage.title)
//    }

//    "display the confirm business page for a non-unhandled exception failure and business user with details" taggedAs UiTag in new WebBrowserForSelenium {
//      go to BeforeYouStartPage
//      cacheConfirmBusinessDetailsSetup()
//      fillWith(registrationNumber = "I1", isCurrentKeeper = false)
//      currentUrl should equal(ConfirmBusinessPage.url)
//    }

  }

  private def cachePreLookupSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .transactionId()
      .bruteForcePreventionViewModel()

  private def cacheConfirmBusinessDetailsSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .transactionId()
      .bruteForcePreventionViewModel()
      .businessDetails()
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
      .setupBusinessDetails()
      .storeBusinessDetailsConsent(consent = "true")
      //fulfilModel must be None

}
