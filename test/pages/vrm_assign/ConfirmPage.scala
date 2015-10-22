package pages.vrm_assign

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, find, go, id}
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_assign.Confirm.ConfirmId
import views.vrm_assign.Confirm.ExitId
import views.vrm_assign.Confirm.GranteeConsentId
import views.vrm_assign.Confirm.KeeperEmailWrapper
import views.vrm_assign.Confirm.SupplyEmailId
import views.vrm_assign.Confirm.SupplyEmail_false
import views.vrm_assign.Confirm.SupplyEmail_true

object ConfirmPage extends Page {

  def address = s"$applicationContext/confirm"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Confirm keeper details"

  def confirm(implicit driver: WebDriver) = find(id(ConfirmId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get

  def GranteeConsent(implicit driver: WebDriver) = find(id(GranteeConsentId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to ConfirmPage
    click on confirm
  }

  def isKeeperEmailHidden(implicit driver: WebDriver): Boolean = {
    val keeperEmailWrapper = find(id(KeeperEmailWrapper))
    keeperEmailWrapper match {
      case Some(field) =>
        val style = field.attribute("style")
        val hasDisplayNone = style map (_.contains("display: none"))
        hasDisplayNone.getOrElse(false)
      case _ => true // Element not detected so is hidden.
    }
  }

  def `supply keeper email`(implicit driver: WebDriver) = find(id(s"${SupplyEmailId}_$SupplyEmail_true")).get

  def `don't supply keeper email`(implicit driver: WebDriver) = find(id(s"${SupplyEmailId}_$SupplyEmail_false")).get
}
