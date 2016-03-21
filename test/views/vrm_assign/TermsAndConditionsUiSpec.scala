package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import org.scalatest.selenium.WebBrowser.{currentUrl, go}
import pages.vrm_assign.TermsAndConditionsPage

final class TermsAndConditionsUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to TermsAndConditionsPage

      currentUrl should equal(TermsAndConditionsPage.url)
    }
  }
}
