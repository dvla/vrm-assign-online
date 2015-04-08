package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.BeforeYouStartPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class BeforeYouStartPageSteps(implicit webDriver: WebBrowserDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `go to BeforeYouStart page` = {
    go to BeforeYouStartPage
    this
  }

  def `click 'Start now' button` = {
    click on BeforeYouStartPage.startNow
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(BeforeYouStartPage.url)
    }
    this
  }
}
