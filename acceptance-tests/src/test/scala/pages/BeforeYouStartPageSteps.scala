package pages

import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.BeforeYouStartPage.startNow
import pages.vrm_assign.BeforeYouStartPage.url
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class BeforeYouStartPageSteps(implicit webDriver: WebBrowserDriver)
  extends helpers.AcceptanceTestHelper {

  def `go to BeforeYouStart page` = {
    go to BeforeYouStartPage
    this
  }

  def `click 'Start now' button` = {
    click on startNow
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
    this
  }
}
