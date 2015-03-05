package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.{eventually, PatienceConfig}
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.VehicleLookupPage._
import pages.vrm_assign.VehicleLookupPage.documentReferenceNumber
import pages.vrm_assign.VehicleLookupPage.keeperPostcode
import pages.vrm_assign.VehicleLookupPage.vehicleRegistrationNumber
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class VehicleLookup_PageSteps(implicit webDriver: WebBrowserDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
    this
  }

  def `happy path` = {
    enter(registrationNumber = "ABC1", docRefNumber = "11111111111", postcode = "SA11AA").
      `find vehicle`
  }

  def `form is filled with the values I previously entered`() = {
    vehicleRegistrationNumber.value should equal("ABC1")
    documentReferenceNumber.value should equal("11111111111")
    keeperPostcode.value should equal("SA11AA")
  }

  def enter(registrationNumber: String, docRefNumber: String, postcode: String) = {
    vehicleRegistrationNumber.value = registrationNumber
    documentReferenceNumber.value = docRefNumber
    keeperPostcode.value = postcode
    this
  }

  def `keeper is acting` = {
    click on currentKeeperYes
    this
  }

  def `keeper is not acting` = {
    click on currentKeeperNo
    this
  }

  def `find vehicle` = {
    click on findVehicleDetails
    this
  }

  def `has error messages` = {
    pageSource contains "Vehicle registration number - Must be valid format"
    pageSource contains "Document reference number - Document reference number must be an 11-digit number"
    this
  }
}
