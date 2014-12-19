package PersonalizedAssignment.StepDefs

import common.commonStepDefs
import cucumber.api.java.en.{Then, Given, When}
import cucumber.api.scala.{EN, ScalaDsl}
import pages.{Payment_PageSteps, Confirm_PageSteps, CaptureCertificateDetails_PageSteps, VehicleLookup_PageSteps}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser._
import org.scalatest.Matchers

class VehiclePersonalAssignmentStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  lazy val User = new commonStepDefs
  lazy val vehicleLookup = new VehicleLookup_PageSteps
  lazy val captureCertificateDetails = new CaptureCertificateDetails_PageSteps
  lazy val confirm = new Confirm_PageSteps
  lazy val payment = new Payment_PageSteps

  @Given("^that I have started the PR Assign Service$")
  def `that_I_have_started_the_PR_Assign_Service`() {
    User.`start the Assign service`
  }

  @Given("^I visit assign web portal$")
  def `i_visit_assign_web_portal`() {
    User.`before you start`

  }

  @When("^I enter data in the \"(.*?)\",\"(.*?)\" and \"(.*?)\" for a vehicle that is eligible for retention$")
  def `i_enter_data_in_the_and_for_a_vehicle_that_is_eligible_for_retention`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) {
    vehicleLookup.enter(vehicleRegistrationNumber, documentReferenceNumber, postcode)
  }

  @When("^I indicate that the keeper is acting$")
  def `i_indicate_that_the_keeper_is_acting`() {
    vehicleLookup.`keeper is acting`
    vehicleLookup.`find vehicle`
  }

  @When("^enter \"(.*?)\",\"(.*?)\",\"(.*?)\",\"(.*?)\"  and \"(.*?)\"$")
  def `enter_and`(box1: String, box2: String, box3: String, box4: String, registrationNumber: String) {
    captureCertificateDetails.`is displayed`
    captureCertificateDetails.`enter certificate details`(box1, box2, box3, box4)
    captureCertificateDetails.`enter registration number`(registrationNumber)
    captureCertificateDetails.`submit details`
  }

  @Then("^the enter confirm details page is displayed and the payment required section is shown$")
  def `the_enter_confirm_details_page_is_displayed_and_the_payment_required_section_is_shown`() {
    confirm.`is displayed`
    confirm.`select consent`
    confirm.`proceed to confirm`
    payment.`is displayed`
  }


}
