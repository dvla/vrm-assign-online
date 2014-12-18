package pages.vrm_assign

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_assign.PaymentPreventBack.ReturnToSuccessId

object PaymentPreventBackPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/payment-prevent-back"

  def url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = ""

  def returnToSuccess(implicit driver: WebDriver): Element = find(id(ReturnToSuccessId)).get
}
