package pages.vrm_assign

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_assign.VrmLocked.ExitassignId
import views.vrm_assign.VrmLocked.NewassignId

object VrmLockedPage extends Page {

  def address = s"$applicationContext/vrm-locked"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title = "Registration mark is locked"

  def newassign(implicit driver: WebDriver) = find(id(NewassignId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitassignId)).get
}
