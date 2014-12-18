package pages.common

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Element, WebBrowserDSL}
import mappings.common.Feedback.FeedbackId
import org.openqa.selenium.WebDriver

object Feedback extends WebBrowserDSL {
  def mailto(implicit driver: WebDriver): Element = find(id(FeedbackId)).get
}