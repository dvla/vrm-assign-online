package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.{PatienceConfig, eventually}
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.ConfirmPaymentPage.confirm
import pages.vrm_assign.ConfirmPaymentPage.url
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class ConfirmPaymentPageSteps(implicit webDriver: WebBrowserDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `happy path` = {
      `confirm the details`
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }(timeout)
    this
  }

  def `confirm the details` = {
    click on confirm
    this
  }

  def `form is filled with the values I previously entered`() = {
    this
  }
}
