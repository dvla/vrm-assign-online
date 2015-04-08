package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.ConfirmPage._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class ConfirmPageSteps(implicit webDriver: WebBrowserDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `happy path` = {
    `is displayed`.
      `select consent`.
      `customer does not want an email`.
      `confirm the details`
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should include(address)
    }(timeout)
    this
  }

  def `select consent` = {
    click on GranteeConsent
    this
  }

  def `confirm the details` = {
    click on confirm
    this
  }

  def `customer does not want an email` = {
    click on `don't supply keeper email`
    this
  }

  def `form is filled with the values I previously entered`() = {
    GranteeConsent.attribute("value") should equal(Some("true"))
    this
  }

  def `form is not filled`() = {
    GranteeConsent.isSelected should equal(false)
    this
  }
}
