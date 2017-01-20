package views.vrm_assign

import composition.TestHarness
import org.scalatest.selenium.WebBrowser.{currentUrl, go}
import pages.vrm_assign.CookiePolicyPage
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}

final class CookiePolicyUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to CookiePolicyPage

      currentUrl should equal(CookiePolicyPage.url)
    }
  }
}
