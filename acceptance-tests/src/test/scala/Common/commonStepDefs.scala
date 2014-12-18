package common

import composition.TestHarness
import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import pages.{VehicleLookup_PageSteps, BeforeYouStart_PageSteps}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

class commonStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers with TestHarness {

  lazy val beforeYouStart = new BeforeYouStart_PageSteps
  lazy val vehicleLookup = new VehicleLookup_PageSteps

  def `start the Assign service` = {
    beforeYouStart.`go to BeforeYouStart page`.
    `is displayed`.
      `click 'Start now' button`
    vehicleLookup.`is displayed`
    this
  }


}
