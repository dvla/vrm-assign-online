package pages

import cucumber.api.scala.{ScalaDsl, EN}
import org.scalatest.Matchers
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.ConfirmPage._


class Confirm_PageSteps (implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers  {

  def `is displayed` = {
    currentUrl should equal(url)
    this
  }

  def `select consent` = {
    click on GranteeConsent
  }

  def `proceed to confirm` = {
    click on confirm
  }


}
