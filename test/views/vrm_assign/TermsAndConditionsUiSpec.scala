package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import org.scalatest.selenium.WebBrowser.{currentUrl, go}
import pages.vrm_assign.TermsAndConditionsPage
import uk.gov.dvla.vehicles.presentation.common.testhelpers.UiTag

final class TermsAndConditionsUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to TermsAndConditionsPage

      currentUrl should equal(TermsAndConditionsPage.url)
    }
  }
}
