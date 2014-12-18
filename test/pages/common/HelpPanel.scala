package pages.common

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Element, WebBrowserDSL}
import mappings.common.Help.HelpId
import org.openqa.selenium.WebDriver

object HelpPanel extends WebBrowserDSL {
  def help(implicit driver: WebDriver): Element = find(id(HelpId)).get
}