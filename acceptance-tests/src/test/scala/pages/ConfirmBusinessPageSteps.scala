package pages

import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.ConfirmBusinessPage.confirm
import pages.vrm_assign.ConfirmBusinessPage.exit
import pages.vrm_assign.ConfirmBusinessPage.url
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class ConfirmBusinessPageSteps(implicit webDriver: WebBrowserDriver)
  extends helpers.AcceptanceTestHelper {

  def `happy path` = {
    `is displayed`
    click on confirm
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
    this
  }

  def `form is filled with the values I previously entered`() = {
    this
  }

  def `form is not filled`() = {
    this
  }

  def `exit the service` = {
    click on exit
    this
  }
}
