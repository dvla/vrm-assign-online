package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser.{click, currentUrl}
import pages.vrm_assign.SetupBusinessDetailsPage.businessAddressWidget
import pages.vrm_assign.SetupBusinessDetailsPage.lookup
import pages.vrm_assign.SetupBusinessDetailsPage.traderContact
import pages.vrm_assign.SetupBusinessDetailsPage.traderEmail
import pages.vrm_assign.SetupBusinessDetailsPage.traderEmailVerify
import pages.vrm_assign.SetupBusinessDetailsPage.traderName
import pages.vrm_assign.SetupBusinessDetailsPage.url
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class SetupBusinessDetailsPageSteps(implicit webDriver: WebBrowserDriver, timeout: PatienceConfig)
  extends ScalaDsl with EN with Matchers {

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
    businessAddressWidget.addressLine1.value = "Forester House, Flat 45"
    businessAddressWidget.addressLine2.value = "Great Hitch Str."
    businessAddressWidget.town.value = "London"
    businessAddressWidget.postcode.value = "SA11AA"
    click on businessAddressWidget.remember.underlying
    click on lookup
    this
  }

  def `form is filled with the values I previously entered` = {
    traderContact.value should equal("VALTECH")
    traderEmail.value should equal("business.example@test.com")
    traderEmailVerify.value should equal("business.example@test.com")
    traderName.value should equal("TRADER-NAME")
    businessAddressWidget.addressLine1.value should equal("Forester House, Flat 45")
    businessAddressWidget.addressLine2.value should equal("Great Hitch Str.")
    businessAddressWidget.town.value should equal("London")
    businessAddressWidget.postcode.value should equal("SA11AA")
    this
  }

  def `form is not filled` = {
    traderContact.value should equal("")
    traderEmail.value should equal("")
    traderEmailVerify.value should equal("")
    traderName.value should equal("")
    businessAddressWidget.addressLine1.value should equal("")
    businessAddressWidget.addressLine2.value should equal("")
    businessAddressWidget.town.value should equal("")
    businessAddressWidget.postcode.value should equal("")
    this
  }
}
