package pages.vrm_assign

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, find, go, id}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_assign.Confirm.{ConfirmId,ExitId,GranteeConsentId}

object ConfirmPaymentPage extends Page {

  def address = buildAppUrl("confirm-payment")

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Confirm keeper details"

  def confirm(implicit driver: WebDriver) = find(id(ConfirmId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get

  def GranteeConsent(implicit driver: WebDriver) = find(id(GranteeConsentId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to ConfirmPaymentPage
    click on confirm
  }
}
