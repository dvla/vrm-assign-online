package pages

import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.PaymentPreventBackPage.address
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class PaymentPreventBackPageSteps(implicit webDriver: WebBrowserDriver)
  extends helpers.AcceptanceTestHelper {

  def `is displayed` = {
    eventually {
      currentUrl should equal(address)
    }
    this
  }
}
