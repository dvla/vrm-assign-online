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
import uk.gov.dvla.vehicles.presentation.common.testhelpers.RandomVrmGenerator

import scala.concurrent.duration.DurationInt

final class VehiclePersonalAssignmentStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  private val timeout = PatienceConfig(timeout = 30.seconds)
  private val beforeYouStart = new BeforeYouStartPageSteps()(webDriver, timeout)
  private val vehicleLookup = new VehicleLookupPageSteps()(webDriver, timeout)
  private val captureCertificateDetails = new CaptureCertificateDetailsPageSteps()(webDriver, timeout)
  private val confirm = new ConfirmPageSteps()(webDriver, timeout)
  private val payment = new PaymentPageSteps()(webDriver, timeout)
  private val vehicleNotFound = new VehicleNotFoundPageSteps()(webDriver, timeout)
  private val vrmLocked = new VrmLockedPageSteps()(webDriver, timeout)
  private val setupBusinessDetails = new SetupBusinessDetailsPageSteps()(webDriver, timeout)
  private val businessChooseYourAddress = new BusinessChooseYourAddressPageSteps()(webDriver, timeout)
  private val confirmBusiness = new ConfirmBusinessPageSteps()(webDriver, timeout)
  private val user = new CommonStepDefs(
    beforeYouStart,
    vehicleLookup,
    vrmLocked,
    captureCertificateDetails,
    setupBusinessDetails,
    businessChooseYourAddress,
    confirmBusiness
  )(webDriver, timeout)

  @Given("^that I have started the PR Assign Service$")
  def `that_I_have_started_the_PR_Assign_Service`() {
    user.`start the Assign service`
  }

  @When("^I enter data in the \"(.*?)\",\"(.*?)\",\"(.*?)\" and \"(.*?)\" for a vehicle that is eligible for retention$")
  def `i enter data in the and for a vehicle that is eligible for retention`(replacementVRN: String, vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) {
    vehicleLookup.enter(replacementVRN, vehicleRegistrationNumber, documentReferenceNumber, postcode)
  }

  @When("^I indicate that the keeper is acting$")
  def `i indicate that the keeper is acting`() {
    vehicleLookup.`keeper is acting`
    vehicleLookup.`find vehicle`
  }

  @When("^I enter certificate \"(.*?)\",\"(.*?)\",\"(.*?)\" and \"(.*?)\"$")
  def `I enter certificate and`(box1: String, box2: String, box3: String, box4: String) {
    captureCertificateDetails.`is displayed`
    captureCertificateDetails.`enter certificate details`(box1, box2, box3, box4)
    captureCertificateDetails.`submit details`
  }

  @Then("^the enter confirm details page is displayed and the payment required section is shown$")
  def `the enter confirm details page is displayed and the payment required section is shown`() {
    confirm.`is displayed`
    confirm.`select consent`
    confirm.`confirm the details`
    payment.`is displayed`
  }

  //Scenario 2
  @When("^I enter invalid data in the \"(.*?)\",\"(.*?)\",\"(.*?)\" and \"(.*?)\" fields$")
  def `i enter invalid data in the and fields`(replacementVRN: String, vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) {
    vehicleLookup
      .enter(replacementVRN, vehicleRegistrationNumber, documentReferenceNumber, postcode)
      .`keeper is not acting`
      .`find vehicle`
  }

  @Then("^the error messages for invalid data in the Vehicle Registration Number, Doc Ref ID and Postcode fields are displayed$")
  def `the error messages for invalid data in the Vehicle Registration Number Doc Ref ID and Postcode fields are displayed`() {
    vehicleLookup.`has error messages`
  }

  //Scenario 3
  @When("^I enter data in the \"(.*?)\",\"(.*?)\",\"(.*?)\" and \"(.*?)\" that does not match a valid vehicle record$")
  def `i enter data in the and that does not match a valid vehicle record`(replacementVRN: String, vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) {
    user.`perform vehicle lookup (trader acting)`(replacementVRN, vehicleRegistrationNumber, documentReferenceNumber, postcode)
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
  @When("^I enter data that does not match a valid vehicle record three times in a row$")
  def `i enter data that does not match a valid vehicle record three times in a row`() {
    val vehicleRegistrationNumber = RandomVrmGenerator.vrm
    val documentReferenceNumber = "22222222222"
    val replacementVRN = "ABC123"

    user.`perform vehicle lookup (trader acting)`(replacementVRN, vehicleRegistrationNumber, documentReferenceNumber, "AA11AA") // 1st
    vehicleNotFound.`is displayed`
    user.goToVehicleLookupPage

    user.`perform vehicle lookup (trader acting)`(replacementVRN, vehicleRegistrationNumber, documentReferenceNumber, "AA11AA") // 2nd
    vehicleNotFound.`is displayed`
    user.goToVehicleLookupPage

    user.`perform vehicle lookup (trader acting)`(replacementVRN, vehicleRegistrationNumber, documentReferenceNumber, "AA11AA") // 3rd
    vehicleNotFound.`is displayed`
    user.goToVehicleLookupPage

    user.`perform vehicle lookup (trader acting)`(replacementVRN, vehicleRegistrationNumber, documentReferenceNumber, "AA11AA") // 4th
  }

  @Then("^the brute force lock out page is displayed$")
  def `the brute force lock out page is displayed`() {
    vrmLocked.`is displayed`
  }

  @Then("^reset the (.*?) so it won't be locked next time we run the tests$")
  def `reset the <vehicle-registration-number> so it won't be locked next time we run the tests`(vehicleRegistrationNumber: String) {
    user.
      goToVehicleLookupPage.
      `perform vehicle lookup (trader acting)`("ABC123", vehicleRegistrationNumber, "11111111111", "SA11AA") // This combination of doc ref and postcode should always appear valid to the legacy stubs, so will reset the brute force count.
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
  @When("^I enter data in the \"(.*?)\",\"(.*?)\",\"(.*?)\" and \"(.*?)\" for a vehicle that is eligible for retention and I indicate that the keeper is not acting and I have not previously chosen to store my details$")
  def `i_enter_data_in_the_and_for_a_vehicle_that_is_eligible_for_retention_and_I_indicate_that_the_keeper_is_not_acting_and_I_have_not_previously_chosen_to_store_my_details`(replacementVRN: String, vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) {
    vehicleLookup.
      enter(replacementVRN, vehicleRegistrationNumber, documentReferenceNumber, postcode).
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
  @When("^I enter data in the \"(.*?)\", \"(.*?)\",\"(.*?)\" and \"(.*?)\" for a vehicle that is eligible for retention and I indicate that the keeper is not acting and I have previously chosen to store my details and the cookie is still fresh less than seven days old$")
  def `I enter data in the <vehicle-registration-number>, <document-reference-number> and <postcode> for a vehicle that and I indicate that the keeper is not acting and I have previously chosen to store my details and the cookie is still fresh less than seven days old`(replacementVRN: String, vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) = {
    // 1st Store the details
    user.
      `perform vehicle lookup (trader acting)`(replacementVRN, vehicleRegistrationNumber, documentReferenceNumber, postcode).
      `provide business details`
    confirmBusiness.`exit the service` // Exit the service

    //2nd validate the details are still stored
    user.`check tracking cookie is fresh`

    beforeYouStart.`go to BeforeYouStart page`.
      `is displayed`
    beforeYouStart.`click 'Start now' button`
    vehicleLookup.`is displayed`
    user.`perform vehicle lookup (trader acting)`(replacementVRN, vehicleRegistrationNumber, documentReferenceNumber, postcode)
  }

  @Then("^the confirm business details page is displayed$")
  def `the_confirm_business_details_page_is_displayed`() {
    confirmBusiness.`is displayed`
  }

  /** DO NOT REMOVE **/
  @After()
  def teardown() = webDriver.quit()
}
