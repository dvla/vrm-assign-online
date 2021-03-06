package pages.vrm_assign

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_assign.ConfirmBusiness.ConfirmId
import views.vrm_assign.ConfirmBusiness.ExitId

object ConfirmBusinessPage extends Page {

  def address = buildAppUrl("confirm-business")

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Confirm your business details"

  def confirm(implicit driver: WebDriver) = find(id(ConfirmId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get
}
