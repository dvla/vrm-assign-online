package pages.vrm_assign

import helpers.webbrowser.Page
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_assign.Success.FinishId
import views.vrm_assign.Success.PrintId

object SuccessPage extends Page {

  def address = s"$applicationContext/success"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Summary"

  def finish(implicit driver: WebDriver) = find(id(FinishId)).get

  def print(implicit driver: WebDriver) = find(id(PrintId)).get
}