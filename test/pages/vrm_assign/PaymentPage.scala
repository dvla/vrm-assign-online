package pages.vrm_assign

import helpers.webbrowser.Page
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_assign.Payment._

object PaymentPage extends Page {

  def address = s"$applicationContext/payment/begin"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Payment details"

  def cancel(implicit driver: WebDriver) = find(id(CancelId)).get
}
