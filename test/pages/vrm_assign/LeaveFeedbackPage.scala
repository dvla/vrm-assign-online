package pages.vrm_assign

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}
import pages.ApplicationContext.applicationContext

object LeaveFeedbackPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/leave-feedback"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = ""
  final val titleCy: String = ""

}
