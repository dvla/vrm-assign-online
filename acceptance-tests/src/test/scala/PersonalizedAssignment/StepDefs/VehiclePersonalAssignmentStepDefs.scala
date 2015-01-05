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
  lazy val vrmLocked = new VrmLocked_PageSteps
  lazy val setupBusinessDetails = new SetupBusinessDetails_PageSteps


  @Given("^that I have started the PR Assign Service$")
  def `that_I_have_started_the_PR_Assign_Service`() {
    user.`start the Assign service`
  }

  //Scenario 1
  @Given("^I visit vehicle assign portal$")
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
    printf("The Totle"+webDriver.getTitle)
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
      .enter(vehicleRegistrationNumber, documentReferenceNumber, postcode)
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
  def `i enter data in the and that does not match a valid vehicle record`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) {
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

  //Scenario 4
  @When("^I enter data in the \"(.*?)\",\"(.*?)\" and \"(.*?)\"  that does not match a valid vehicle record three times in a row$")
  def `i enter data in the and that does not match a valid vehicle record three times in a row`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) {
    user.`vehicleLookupDoesNotMatchRecord`(vehicleRegistrationNumber, documentReferenceNumber, postcode).
      //`enterCertificateDetails`.
      `goToVehicleLookupPage`

      .`vehicleLookupDoesNotMatchRecord`(vehicleRegistrationNumber, documentReferenceNumber, postcode).
      //`enterCertificateDetails`.
      `goToVehicleLookupPage`.

      `vehicleLookupDoesNotMatchRecord`(vehicleRegistrationNumber, documentReferenceNumber, postcode)
  }

  @Then("^the brute force lock out page is displayed$")
  def `the brute force lock out page is displayed`() {
    vrmLocked
    user.`quit the browser`
  }

  //Scenario 5
  @When("^I enter data in the \"(.*?)\",\"(.*?)\" and \"(.*?)\" for a vehicle that has a marker set$")
  def `i enter data in the and for a vehicle that has a marker set`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) {
    //    vehicleLookup.
    //      enter(vehicleRegistrationNumber, documentReferenceNumber, postcode).
    //      `keeper is acting`.
    //      `find vehicle`
    //    user.`enterCertificateDetails`
  }

  @Then("^the direct to paper channel page is displayed$")
  def `the direct to paper channel page is displayed`() {
    //    vehicleNotFound.
    //      `is displayed`.
    //      `has 'direct to paper' message`
    user.`quit the browser`
  }

  //Scenario 6
  @When("^I enter data in the \"(.*?)\",\"(.*?)\" and \"(.*?)\" for a vehicle that is not eligible for retention$")
  def `i enter data in the and for a vehicle that is not eligible for retention`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) {
    //    vehicleLookup.
    //      enter(vehicleRegistrationNumber, documentReferenceNumber, postcode).
    //      `keeper is acting`.
    //      `find vehicle`
    //    user.`enterCertificateDetails`
  }

  @Then("^the vehicle not eligible page is displayed$")
  def `the vehicle not eligible page is displayed`() {
    //    vehicleNotFound.
    //      `is displayed`.
    //      `has 'not found' message`
    user.`quit the browser`
  }

  //Scenario 7
  @When("^I enter data in the \"(.*?)\",\"(.*?)\" and \"(.*?)\" for a vehicle that is eligible for retention and I indicate that the keeper is not acting and I have not previously chosen to store my details$")
  def `i_enter_data_in_the_and_for_a_vehicle_that_is_eligible_for_retention_and_I_indicate_that_the_keeper_is_not_acting_and_I_have_not_previously_chosen_to_store_my_details`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) {
    vehicleLookup.
      enter(vehicleRegistrationNumber, documentReferenceNumber, postcode).
      `keeper is not acting`.
      `find vehicle`
  }

  @Then("^the supply business details page is displayed$")
  def `the_supply_business_details_page_is_displayed`() {
    setupBusinessDetails.`is displayed`
    user.`quit the browser`
  }

  //Scenario 8
  @When("^I enter data in the \"(.*?)\",\"(.*?)\" and \"(.*?)\" for a vehicle that is eligible for retention and I indicate that the keeper is not acting and I have previously chosen to store my details and the cookie is still fresh less than seven days old\\)$")
  def `i_enter_data_in_the_and_for_a_vehicle_that_is_eligible_for_retention_and_I_indicate_that_the_keeper_is_not_acting_and_I_have_previously_chosen_to_store_my_details_and_the_cookie_is_still_fresh_less_than_seven_days_old`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) {
    //1st Store the details
    user.
      goToVehicleLookupPageWithNonKeeper(vehicleRegistrationNumber, documentReferenceNumber, postcode).
      provideBusinessDetails.
      chooseBusinessAddress.
      confirmBusinessDetailsIsDisplayed.
      storeBusinessDetails.
      exitBusiness.
      validateCookieIsFresh.

      //2nd validate details are stored
      goToVehicleLookupPage.
      goToVehicleLookupPageWithNonKeeper(vehicleRegistrationNumber, documentReferenceNumber, postcode)
  }

  @Then("^the confirm business details page is displayed$")
  def `the_confirm_business_details_page_is_displayed`() {
    user.confirmBusinessDetailsIsDisplayed
    user.`quit the browser`
  }

}
