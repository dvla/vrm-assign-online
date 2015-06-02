package pages.vrm_assign

import helpers.webbrowser.Page
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_assign.BeforeYouStart.NextId

object BeforeYouStartPage extends Page {

  def address = s"$applicationContext/before-you-start"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Put a registration number on a vehicle"
  final val titleCy: String = "Cael gwared cerbyd i mewn i'r fasnach foduron"

  def startNow(implicit driver: WebDriver) = find(id(NextId)).get
}
