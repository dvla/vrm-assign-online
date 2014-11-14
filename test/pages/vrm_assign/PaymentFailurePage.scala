package pages.vrm_assign

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_assign.PaymentFailure._

object PaymentFailurePage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/payment-failure"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Payment Failure"

  def tryAgain(implicit driver: WebDriver): Element = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}
