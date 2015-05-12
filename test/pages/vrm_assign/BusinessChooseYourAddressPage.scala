package pages.vrm_assign

import helpers.webbrowser.Page
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.Select
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_assign.BusinessChooseYourAddress.AddressSelectId
import views.vrm_assign.BusinessChooseYourAddress.EnterAddressManuallyButtonId
import views.vrm_assign.BusinessChooseYourAddress.SelectId
import views.vrm_assign.Main.BackId

object BusinessChooseYourAddressPage extends Page {

  final val address: String = s"$applicationContext/business-choose-your-address"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title = "Select your business address"
  final val titleCy = "Dewiswch eich cyfeiriad masnach"

  def chooseAddress(implicit driver: WebDriver) = singleSel(id(AddressSelectId))

  def manualAddress(implicit driver: WebDriver) = find(id(EnterAddressManuallyButtonId)).get

  def getList(implicit driver: WebDriver) = {
    val select = new Select(driver.findElement(By.id(AddressSelectId)))
    select.getOptions
  }

  def getListCount(implicit driver: WebDriver): Int = getList.size()

  def select(implicit driver: WebDriver) = find(id(SelectId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    // HACK for Northern Ireland
    //    chooseAddress.value = traderUprnValid.toString
    chooseAddress.value = "0"
    click on select
  }

  def sadPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    click on select
  }
}
