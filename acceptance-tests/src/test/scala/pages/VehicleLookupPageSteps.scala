package pages

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

final class VehicleLookupPageSteps(implicit webDriver: WebBrowserDriver)
  extends helpers.AcceptanceTestHelper {

  def `happy path for business`(regNo: String = "A2") = {
    `is displayed`.
      enter(replacementVRN = "ABC123", registrationNumber = regNo, docRefNumber = "11111111111", postcode = "SA11AA").
      `keeper is not acting`.
      `find vehicle`
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
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

  def `happy path for keeper`(regNo: String = "A2") = {
    enter(replacementVRN = "ABC123", registrationNumber = regNo, docRefNumber = "11111111111", postcode = "SA11AA").
      `keeper is acting`.
      `find vehicle`
    this
  }

  def `keeper is acting` = {
    click on currentKeeperYes
    this
  }

  def `form is filled with the values I previously entered`(regNo: String = "A2") = {
    vehicleRegistrationNumber.value should equal(regNo)
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
    pageSource should include("Vehicle registration number - Vehicle registration number must be valid format")
    pageSource should include("Document reference number - Document reference number must be an 11-digit number")
    this
  }
}
