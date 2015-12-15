package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser.{click, currentUrl, pageSource}
import pages.vrm_assign.VehicleLookupPage.currentKeeperNo
import pages.vrm_assign.VehicleLookupPage.currentKeeperYes
import pages.vrm_assign.VehicleLookupPage.findVehicleDetails
import pages.vrm_assign.VehicleLookupPage.replacementVRNTag
import pages.vrm_assign.VehicleLookupPage.url
import pages.vrm_assign.VehicleLookupPage.documentReferenceNumber
import pages.vrm_assign.VehicleLookupPage.keeperPostcode
import pages.vrm_assign.VehicleLookupPage.vehicleRegistrationNumber
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import uk.gov.dvla.vehicles.presentation.common.testhelpers.RandomVrmGenerator

final class VehicleLookupPageSteps(implicit webDriver: WebBrowserDriver, timeout: PatienceConfig)
  extends ScalaDsl with EN with Matchers {

  private val replacementVRN = RandomVrmGenerator.vrm

  def `happy path for business` = {
    `is displayed`.
      enter(replacementVRN = "ABC123", registrationNumber = "DD22", docRefNumber = "11111111111", postcode = "SA11AA").
      `keeper is not acting`.
      `find vehicle`
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }(timeout)
    this
  }

  def `keeper is not acting` = {
    click on currentKeeperNo
    this
  }

  def enter(replacementVRN: String, registrationNumber: String, docRefNumber: String, postcode: String) = {
    replacementVRNTag.value = replacementVRN
    vehicleRegistrationNumber.value = registrationNumber
    documentReferenceNumber.value = docRefNumber
    keeperPostcode.value = postcode
    this
  }

  def `find vehicle` = {
    click on findVehicleDetails
    this
  }

  def `happy path for keeper` = {
    enter(replacementVRN = "ABC123", registrationNumber = "DD22", docRefNumber = "11111111111", postcode = "SA11AA").
      `keeper is acting`.
      `find vehicle`
    this
  }

  def `keeper is acting` = {
    click on currentKeeperYes
    this
  }

  def `form is filled with the values I previously entered`() = {
    vehicleRegistrationNumber.value should equal("DD22")
    documentReferenceNumber.value should equal("11111111111")
    keeperPostcode.value should equal("SA11AA")
    this
  }

  def `form is not filled`() = {
    vehicleRegistrationNumber.value should equal("")
    documentReferenceNumber.value should equal("")
    keeperPostcode.value should equal("")
    this
  }

  def `has error messages` = {
    pageSource contains "Vehicle registration number - Must be valid format"
    pageSource contains "Document reference number - Document reference number must be an 11-digit number"
    this
  }
}
