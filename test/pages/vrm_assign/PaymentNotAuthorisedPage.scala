package pages.vrm_assign

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_assign.PaymentNotAuthorised._

object PaymentNotAuthorisedPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/payment-not-authorised"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Payment Cancelled or Not Authorised"

  def tryAgain(implicit driver: WebDriver): Element = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}
