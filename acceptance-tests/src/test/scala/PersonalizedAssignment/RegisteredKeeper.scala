package PersonalizedAssignment

import cucumber.api.scala.{EN, ScalaDsl}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.scalatest.Matchers


final class RegisteredKeeper(webBrowserDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

}
