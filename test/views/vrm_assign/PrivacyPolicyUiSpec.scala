package views.vrm_assign

import composition.TestHarness
import org.scalatest.selenium.WebBrowser.{currentUrl, go}
import pages.vrm_assign.PrivacyPolicyPage
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}

final class PrivacyPolicyUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to PrivacyPolicyPage

      currentUrl should equal(PrivacyPolicyPage.url)
    }
  }
}
