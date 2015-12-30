package pages

import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser.{click, currentUrl}
import pages.vrm_assign.ConfirmPage.{confirm, `don't supply keeper email`, GranteeConsent, url}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class ConfirmPageSteps(implicit webDriver: WebBrowserDriver)
  extends helpers.AcceptanceTestHelper {

  def `happy path` = {
    `is displayed`.
      `select consent`.
      `customer does not want an email`.
      `confirm the details`
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
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
