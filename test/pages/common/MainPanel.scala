package pages.common

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Element, WebBrowserDSL}
import org.openqa.selenium.WebDriver
import views.vrm_assign.Main.BackId

object MainPanel extends WebBrowserDSL {

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get
}