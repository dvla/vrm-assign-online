package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.BeforeYouStartPage.startNow
import pages.vrm_assign.BeforeYouStartPage.url
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class BeforeYouStartPageSteps(implicit webDriver: WebBrowserDriver, timeout: PatienceConfig)
  extends ScalaDsl with EN with Matchers {

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
    }(timeout)
    this
  }
}
