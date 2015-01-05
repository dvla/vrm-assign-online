package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.VrmLockedPage._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

class VrmLocked_PageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers{

  def `is displayed` = {
    currentUrl should equal(url)
    this
  }


}
