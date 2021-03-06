package pages.vrm_assign

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_assign.BeforeYouStart.NextId

object BeforeYouStartPage extends Page {

  def address = buildAppUrl("before-you-start")

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Put a registration number on a vehicle"
  final val titleCy: String = "Cymryd rhif cofrestru oddi ar gerbyd"

  def startNow(implicit driver: WebDriver) = find(id(NextId)).get

  def footer(implicit driver: WebDriver) = driver.findElement(By.id("footer"))

  def footerMetaInner(implicit driver: WebDriver) = footer.findElement(By.className("footer-meta-inner"))

  def footerItem(index: Int)(implicit driver: WebDriver) = footerMetaInner.findElements(By.tagName    ("li")).get(index)
}
