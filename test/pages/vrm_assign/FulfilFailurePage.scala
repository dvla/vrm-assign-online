package pages.vrm_assign

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_assign.FulfilFailure.ExitId

object FulfilFailurePage extends Page {

  def address = s"$applicationContext/fulfil-failure"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Transaction not successful"

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get
}
