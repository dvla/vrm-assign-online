package pages.vrm_assign

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_assign.Error.StartAgainId

object ErrorPage extends Page {

  def address = buildAppUrl("error/stubbed-exception-digest")

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title = "We are sorry"

  def startAgain(implicit driver: WebDriver) = find(id(StartAgainId)).get
}
