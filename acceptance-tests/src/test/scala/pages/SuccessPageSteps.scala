package pages

import org.scalatest.selenium.WebBrowser.{currentUrl, find, Element, id}
import pages.vrm_assign.SuccessPage.url
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class SuccessPageSteps(implicit webDriver: WebBrowserDriver)
  extends helpers.AcceptanceTestHelper {

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
    this
  }

  def `has pdf link` = {
    val element: Option[Element] = find(id("create-pdf"))
    element match {
      case Some(e) =>
        e should be ('displayed)
      case None => element should be (defined)
    }
    this
  }
}
