package common

import composition.TestHarness
import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import pages.{VehicleLookup_PageSteps, BeforeYouStart_PageSteps}
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

class commonStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers with TestHarness {

  lazy val beforeYouStart = new BeforeYouStart_PageSteps
  lazy val vehicleLookup = new VehicleLookup_PageSteps

  def `start the Assign service`{
    val TestUrl = "test.url"
    val value = s"http://localhost:9000/"
    Logger.debug(s"configureTestUrl - Set system property ${TestUrl} to value $value")
    sys.props += ((TestUrl, value))
  }

  def `before you start` {
    beforeYouStart.`go to BeforeYouStart page`.
      `is displayed`.
      `click 'Start now' button`
    vehicleLookup.`is displayed`
    this
  }



}
