package pages.vrm_assign

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}
import pages.ApplicationContext.applicationContext

object PaymentCallbackPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/payment/callback"

  def url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Payment processing"
}
