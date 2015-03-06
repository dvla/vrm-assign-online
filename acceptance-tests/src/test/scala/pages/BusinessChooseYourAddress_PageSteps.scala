package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.BusinessChooseYourAddressPage
import pages.vrm_assign.BusinessChooseYourAddressPage._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class BusinessChooseYourAddress_PageSteps(implicit webDriver: WebBrowserDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `proceed to next page` = {
    eventually {
      currentUrl should equal(url)
      pageTitle should equal(title)
    }
    BusinessChooseYourAddressPage.chooseAddress.value = "0"
    org.scalatest.selenium.WebBrowser.click on BusinessChooseYourAddressPage.select
    this
  }
}
