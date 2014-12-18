package pages.vrm_assign

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.vrm_assign.VrmLocked
import VrmLocked.{ExitassignId, NewassignId}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext

object VrmLockedPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/vrm-locked"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title = "Registration mark is locked"

  def newassign(implicit driver: WebDriver): Element = find(id(NewassignId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitassignId)).get
}
