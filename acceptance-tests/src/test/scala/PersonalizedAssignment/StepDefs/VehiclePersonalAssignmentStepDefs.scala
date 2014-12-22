package PersonalizedAssignment.StepDefs

import _root_.common.commonStepDefs
import cucumber.api.java.en.{Given, Then, When}
import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import pages._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser._

class VehiclePersonalAssignmentStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  lazy val user = new commonStepDefs
  lazy val vehicleLookup = new VehicleLookup_PageSteps
  lazy val captureCertificateDetails = new CaptureCertificateDetails_PageSteps
  lazy val confirm = new Confirm_PageSteps
  lazy val payment = new Payment_PageSteps
  lazy val vehicleNotFound = new VehicleNotFound_PageSteps

  @Given("^that I have started the PR Assign Service$")
  def `that_I_have_started_the_PR_Assign_Service`() {
    user.`start the Assign service`
  }

  //Scenario 1
  @Given("^I visit assign web portal$")
  def `i visit assign web portal`() {
    user.`before you start`

  }

  @When("^I enter data in the \"(.*?)\",\"(.*?)\" and \"(.*?)\" for a vehicle that is eligible for retention$")
  def `i enter data in the and for a vehicle that is eligible for retention`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) {
    vehicleLookup.enter(vehicleRegistrationNumber, documentReferenceNumber, postcode)
  }

  @When("^I indicate that the keeper is acting$")
  def `i indicate that the keeper is acting`() {
    vehicleLookup.`keeper is acting`
    vehicleLookup.`find vehicle`
  }

  @When("^enter \"(.*?)\",\"(.*?)\",\"(.*?)\",\"(.*?)\"  and \"(.*?)\"$")
  def `enter and`(box1: String, box2: String, box3: String, box4: String, registrationNumber: String) {
    captureCertificateDetails.`is displayed`
    captureCertificateDetails.`enter certificate details`(box1, box2, box3, box4)
    captureCertificateDetails.`enter registration number`(registrationNumber)
    captureCertificateDetails.`submit details`
  }

  @Then("^the enter confirm details page is displayed and the payment required section is shown$")
  def `the enter confirm details page is displayed and the payment required section is shown`() {
    confirm.`is displayed`
    confirm.`select consent`
    confirm.`proceed to confirm`
    payment.`is displayed`
    user.`quit the browser`

  }

  //Scenario 2
  @When("^I enter invalid data in the \"(.*?)\",\"(.*?)\" and \"(.*?)\" fields$")
  def `i enter invalid data in the and fields`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) {
    vehicleLookup
    .enter(vehicleRegistrationNumber,documentReferenceNumber,postcode)
    .`keeper is not acting`
    .`find vehicle`
  }

  @Then("^the error messages for invalid data in the Vehicle Registration Number, Doc Ref ID and Postcode fields are displayed$")
  def `the error messages for invalid data in the Vehicle Registration Number Doc Ref ID and Postcode fields are displayed`() {
    vehicleLookup.`has error messages`
    user.`quit the browser`
  }

  //Scenario 3
  @When("^I enter data in the \"(.*?)\",\"(.*?)\" and \"(.*?)\" that does not match a valid vehicle record$")
  def `i enter data in the and that does not match a valid vehicle record`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String)  {
    vehicleLookup.
      enter(vehicleRegistrationNumber, documentReferenceNumber, postcode).
      `keeper is acting`.
      `find vehicle`
  }

  @Then("^the vehicle not found page is displayed$")
  def `the vehicle not found page is displayed`() {
    vehicleNotFound.`is displayed`
    .`has 'not found' message`
    user.`quit the browser`

  }






}
