package personalizedAssignment.stepDefs

import _root_.common.CommonStepDefs
import cucumber.api.java.After
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import pages._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser._

import scala.concurrent.duration.DurationInt

final class VehiclePersonalAssignmentStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  lazy val beforeYouStart = new BeforeYouStart_PageSteps()(webDriver, timeout)
  lazy val vehicleLookup = new VehicleLookup_PageSteps()(webDriver, timeout)
  lazy val captureCertificateDetails = new CaptureCertificateDetails_PageSteps()(webDriver, timeout)
  lazy val confirm = new Confirm_PageSteps()(webDriver, timeout)
  lazy val payment = new Payment_PageSteps()(webDriver, timeout)
  lazy val vehicleNotFound = new VehicleNotFound_PageSteps()(webDriver, timeout)
  lazy val vrmLocked = new VrmLocked_PageSteps()(webDriver, timeout)
  lazy val setupBusinessDetails = new SetupBusinessDetails_PageSteps()(webDriver, timeout)
  lazy val businessChooseYourAddress = new BusinessChooseYourAddress_PageSteps()(webDriver, timeout)
  lazy val user = new CommonStepDefs(
    beforeYouStart,
    vehicleLookup,
    vrmLocked,
    captureCertificateDetails,
    setupBusinessDetails,
    businessChooseYourAddress
  )(webDriver, timeout)
  implicit val timeout = PatienceConfig(timeout = 30.seconds)

  @Given("^that I have started the PR Assign Service$")
  def `that_I_have_started_the_PR_Assign_Service`() {
    user.`start the Assign service`
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

  @When("^I enter certificate \"(.*?)\",\"(.*?)\",\"(.*?)\",\"(.*?)\" and \"(.*?)\"$")
  def `I enter certificate and`(box1: String, box2: String, box3: String, box4: String, registrationNumber: String) {
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
  }

  //Scenario 3
  @When("^I enter data in the \"(.*?)\",\"(.*?)\" and \"(.*?)\" that does not match a valid vehicle record$")
  def `i enter data in the and that does not match a valid vehicle record`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) {
    vehicleLookup.
      enter(vehicleRegistrationNumber, documentReferenceNumber, postcode).
      `keeper is acting`.
      `find vehicle`
  }

  @Then("^the vrm not found page is displayed$")
  def `the vrm not found page is displayed`() {
    vehicleNotFound.`is displayed`
      .`has 'not found' message`
  }

  @Then("^the doc ref mismatch page is displayed$")
  def `the doc ref mismatch page is displayed`() {
    vehicleNotFound.`is displayed`
      .`has 'doc ref mismatch' message`
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
    vrmLocked.`is displayed`
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
  }

  @Then("^the confirm details page is displayed$")
  def `the_confirm_details_page_is_displayed`() {
    confirm.`is displayed`
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
  }

  /** DO NOT REMOVE **/
  @After()
  def teardown() = webDriver.quit()
}
