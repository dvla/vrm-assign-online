package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.ConfirmBusinessPage
import pages.vrm_assign.ConfirmBusinessPage.confirm
import pages.vrm_assign.ConfirmBusinessPage.rememberDetails
import pages.vrm_assign.ConfirmBusinessPage.exit
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class ConfirmBusinessPageSteps(implicit webDriver: WebBrowserDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `happy path` = {
    `is displayed`
    click on rememberDetails
    click on confirm
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should include(ConfirmBusinessPage.address)
    }(timeout)
    this
  }

  def `form is filled with the values I previously entered`() = {
    rememberDetails.isSelected should equal(true)
    this
  }

  def `form is not filled`() = {
    rememberDetails.isSelected should equal(false)
    this
  }

  def `exit the service` = {
    click on exit
    this
  }
}
