package pages.vrm_assign

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.vrm_assign.VehicleLookupFailure
import VehicleLookupFailure.{ExitId, VehicleLookupId}
import pages.ApplicationContext.applicationContext
import org.openqa.selenium.WebDriver

object VehicleLookupFailurePage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/vehicle-lookup-failure"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Look-up was unsuccessful"
  final val directToPaperTitle: String = "This registration number can not be retained online"
  final val failureTitle: String = "This registration number can not be retained"

  def tryAgain(implicit driver: WebDriver): Element = find(id(VehicleLookupId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}
