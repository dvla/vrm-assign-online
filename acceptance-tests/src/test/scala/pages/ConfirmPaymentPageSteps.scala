package pages

import org.scalatest.selenium.WebBrowser.{click, currentUrl}
import pages.vrm_assign.ConfirmPaymentPage.confirm
import pages.vrm_assign.ConfirmPaymentPage.url
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class ConfirmPaymentPageSteps(implicit webDriver: WebBrowserDriver)
  extends helpers.AcceptanceTestHelper {

  def `happy path` = {
      `confirm the details`
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
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
