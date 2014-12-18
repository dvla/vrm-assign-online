package pages.vrm_assign

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_assign.Success.FinishId

object SuccessPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/success"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Summary"

  def finish(implicit driver: WebDriver): Element = find(id(FinishId)).get
}