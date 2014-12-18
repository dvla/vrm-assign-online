package pages.vrm_assign

import helpers.webbrowser.Page
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_assign.VehicleLookupFailure.{ExitId, VehicleLookupId}

object VehicleLookupFailurePage extends Page {

  def address = s"$applicationContext/vehicle-lookup-failure"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Look-up was unsuccessful"
  final val directToPaperTitle: String = "This registration number can not be assigned online"
  final val failureTitle: String = "This registration number can not be assigned"

  def tryAgain(implicit driver: WebDriver) = find(id(VehicleLookupId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get
}
