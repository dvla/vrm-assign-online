package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.SetupBusinessDetailsPage._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class SetupBusinessDetailsPageSteps(implicit webDriver: WebBrowserDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `happy path` = {
    `is displayed`.
      `enter business details`
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }(timeout)
    this
  }

  def `enter business details` = {
    traderContact.value = "VALTECH"
    traderEmail.value = "business.example@test.com"
    traderEmailVerify.value = "business.example@test.com"
    traderName.value = "TRADER-NAME"
    traderPostcode.value = "SA11AA"
    click on lookup
    this
  }

  def `form is filled with the values I previously entered` = {
    traderContact.value should equal("VALTECH")
    traderEmail.value should equal("business.example@test.com")
    traderEmailVerify.value should equal("business.example@test.com")
    traderName.value should equal("TRADER-NAME")
    traderPostcode.value should equal("SA11AA")
    this
  }

  def `form is not filled` = {
    traderContact.value should equal("")
    traderEmail.value should equal("")
    traderEmailVerify.value should equal("")
    traderName.value should equal("")
    traderPostcode.value should equal("")
    this
  }
}
