package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually._
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.ConfirmPage._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

class Confirm_PageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

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

  def `proceed to confirm` = {
    click on confirm
    this
  }
}
