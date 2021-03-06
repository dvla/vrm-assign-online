package pages.vrm_assign

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_assign.PaymentFailure.{ExitId, TryAgainId}

object PaymentFailurePage extends Page {

  def address = buildAppUrl("payment-failure")

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Payment failure"

  def tryAgain(implicit driver: WebDriver) = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get
}
