package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually._
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.BeforeYouStartPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

class BeforeYouStart_PageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

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
