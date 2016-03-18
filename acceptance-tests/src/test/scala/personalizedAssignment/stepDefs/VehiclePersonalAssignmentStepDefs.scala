package personalizedAssignment.stepDefs

import _root_.common.CommonStepDefs
import cucumber.api.java.After
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import pages.BeforeYouStartPageSteps
import pages.CaptureCertificateDetailsPageSteps
import pages.ConfirmBusinessPageSteps
import pages.ConfirmPageSteps
import pages.ConfirmPaymentPageSteps
import pages.PaymentPageSteps
import pages.SetupBusinessDetailsPageSteps
import pages.SuccessPageSteps
import pages.VehicleNotFoundPageSteps
import pages.VehicleLookupPageSteps
import pages.VrmLockedPageSteps
import uk.gov.dvla.vehicles.presentation.common
import common.helpers.webbrowser.WebBrowserDriver
import common.testhelpers.RandomVrmGenerator

final class VehiclePersonalAssignmentStepDefs(implicit webDriver: WebBrowserDriver)
  extends helpers.AcceptanceTestHelper {

  private val beforeYouStart = new BeforeYouStartPageSteps()
  private val vehicleLookup = new VehicleLookupPageSteps()
  private val captureCertificateDetails = new CaptureCertificateDetailsPageSteps()
  private val confirm = new ConfirmPageSteps()
  private val confirmPayment = new ConfirmPaymentPageSteps()
  private val payment = new PaymentPageSteps()
  private val success = new SuccessPageSteps()
  private val vehicleNotFound = new VehicleNotFoundPageSteps()
  private val vrmLocked = new VrmLockedPageSteps()
  private val setupBusinessDetails = new SetupBusinessDetailsPageSteps()
  private val confirmBusiness = new ConfirmBusinessPageSteps()
  private val user = new CommonStepDefs(
    beforeYouStart,
    vehicleLookup,
    vrmLocked,
    captureCertificateDetails,
    setupBusinessDetails,
    confirmBusiness
  )

  @Then("^the contact information is displayed$")
  def `the contact information is displayed`() {
    vehicleNotFound.`has contact information`()
  }

  @Then("^the contact information is not displayed$")
  def `the contact information is not displayed`() {
    vehicleNotFound.`has no contact information`()
  }

  @Then("^the replacement VRN and the current registration are correctly formatted")
  def `the replacement VRN and the current registration are correctly formatted`() {
    vehicleNotFound.`displays the formatted registration numbers`()
  }

  @Given("^that I have started the PR Assign Service$")
  def `that_I_have_started_the_PR_Assign_Service`() {
    user.`start the Assign service`
  }

  @When("^I enter data in the \"(.*?)\",\"(.*?)\",\"(.*?)\" and \"(.*?)\" for a " +
    "vehicle that is eligible for retention$")
  def `i enter data in the and for a vehicle that is eligible for retention`(replacementVRN: String,
                                                                              vehicleRegistrationNumber: String,
                                                                              documentReferenceNumber: String,
                                                                              postcode: String) {
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
  def `i enter invalid data in the and fields`(replacementVRN: String,
                                               vehicleRegistrationNumber: String,
                                               documentReferenceNumber: String,
                                               postcode: String) {
    vehicleLookup
      .enter(replacementVRN, vehicleRegistrationNumber, documentReferenceNumber, postcode)
      .`keeper is not acting`
      .`find vehicle`
  }

  @Then("^the error messages for invalid data in the Vehicle Registration Number, " +
    "Doc Ref ID and Postcode fields are displayed$")
  def `the error messages for invalid data in the Vehicle Registration Number Doc Ref ID and Postcode fields are displayed`() {
    vehicleLookup.`has error messages`
  }

  //Scenario 3
  @When("^I enter data in the \"(.*?)\",\"(.*?)\",\"(.*?)\" and \"(.*?)\" that does not match a valid vehicle record$")
  def `i enter data in the and that does not match a valid vehicle record`(replacementVRN: String,
                                                                           vehicleRegistrationNumber: String,
                                                                           documentReferenceNumber: String,
                                                                           postcode: String) {
    user.`perform vehicle lookup (trader acting)`(replacementVRN,
      vehicleRegistrationNumber,
      documentReferenceNumber,
      postcode
    )
  }

  @When("^I enter data in the \"(.*?)\",\"(.*?)\",\"(.*?)\" and \"(.*?)\" that does match a valid vehicle record$")
  def `i enter data in the and that does match a valid vehicle record`(replacementVRN: String,
                                                                           vehicleRegistrationNumber: String,
                                                                           documentReferenceNumber: String,
                                                                           postcode: String) {
    user.`perform vehicle lookup (trader acting)`(replacementVRN,
      vehicleRegistrationNumber,
      documentReferenceNumber,
      postcode
    )
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

  @Then("^the postcode mismatch page is displayed$")
  def `the postcode mismatch page is displayed`() {
    vehicleNotFound.`is displayed`
      .`has 'postcode mismatch' message`
  }

  //Scenario 4
  @When("^I enter data in the \"(.*?)\",\"(.*?)\" and \"(.*?)\" for a vehicle that is not eligible for retention$")
  def `i enter data in the and for a vehicle that is not eligible for retention`(vehicleRegistrationNumber: String,
                                                                                 documentReferenceNumber: String,
                                                                                 postcode: String) {
    vehicleLookup.
      enter(RandomVrmGenerator.uniqueVrm, vehicleRegistrationNumber, documentReferenceNumber, postcode).
      `keeper is acting`.
      `find vehicle`
      user.`enterCertificateDetails`
  }

  @Then("^the vehicle not eligible page is displayed$")
  def `the vehicle not eligible page is displayed`() {
    vehicleNotFound.
      `is displayed`.
      `has 'not eligible' message`
  }

  //Scenario 5
  @When("^I enter data in the \"(.*?)\",\"(.*?)\" and \"(.*?)\" for a vehicle that has a marker set$")
  def `i enter data in the and for a vehicle that has a marker set`(vehicleRegistrationNumber: String,
                                                                    documentReferenceNumber: String,
                                                                    postcode: String) {
    vehicleLookup.
      enter(RandomVrmGenerator.uniqueVrm, vehicleRegistrationNumber, documentReferenceNumber, postcode).
      `keeper is acting`.
      `find vehicle`
    user.`enterCertificateDetails`
  }

  @Then("^the direct to paper channel page is displayed$")
  def `the direct to paper channel page is displayed`() {
    vehicleNotFound.
      `is displayed`.
      `has 'direct to paper' message`
  }

  //Scenario 6
  @When("^I enter data that does not match a valid vehicle record three times in a row$")
  def `i enter data that does not match a valid vehicle record three times in a row`() {
    val replacementVRN = RandomVrmGenerator.vrm
    val vehicleRegistrationNumber = RandomVrmGenerator.vrm
    val documentReferenceNumber = "22222222222"

    for (_ <- 1 to 3) {
      user.`perform vehicle lookup (trader acting)`(replacementVRN,
        vehicleRegistrationNumber,
        documentReferenceNumber,
        "AA11AA"
      )
      vehicleNotFound.`is displayed`
      user.goToVehicleLookupPage
    }

    user.`perform vehicle lookup (trader acting)`(replacementVRN,
      vehicleRegistrationNumber,
      documentReferenceNumber,
      "AA11AA"
    )
  }

  @Then("^the brute force lock out page is displayed$")
  def `the brute force lock out page is displayed`() {
    vrmLocked.`is displayed`
  }

  @Then("^reset the \"(.*?)\" so it won't be locked next time we run the tests$")
  def `reset the vrm so it won't be locked next time we run the tests`(vehicleRegistrationNumber: String) {
    // Current and Replacement VRM are both subject to the brute force service.
    // The brute force count needs to be reset on both.
    user.
      goToVehicleLookupPage.
      `perform vehicle lookup (trader acting)`("ABC123", vehicleRegistrationNumber, "11111111111", "SA11AA")
    // The replacement VRM needs to get past the capture certificate details page for the count to be reset
    user.goToVehicleLookupPage
    vehicleLookup.enter(vehicleRegistrationNumber, "ABC123", "11111111111", "SA11AA")
    `i indicate that the keeper is acting`()
    `I enter certificate and`("1", "14316", "054027", vehicleRegistrationNumber)
  }

  //Scenario 7
  @When("^I enter data in the \"(.*?)\",\"(.*?)\",\"(.*?)\" and \"(.*?)\" for a vehicle that is eligible " +
    "for retention and I indicate that the keeper is not acting and I have not previously chosen to store my details$")
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
  @When("^I enter data in the \"(.*?)\",\"(.*?)\",\"(.*?)\" and \"(.*?)\" for a vehicle that is " +
    "eligible for retention and I indicate that the keeper is not acting and I have " +
    "previously chosen to store my details and the cookie is still fresh less than seven days old$")
  def `I enter data in the <vehicle-registration-number>, <document-reference-number> and <postcode> for a vehicle that and I indicate that the keeper is not acting and I have previously chosen to store my details and the cookie is still fresh less than seven days old`
  (replacementVRN: String,
   vehicleRegistrationNumber: String,
   documentReferenceNumber: String,
   postcode: String
    ) = {
    // 1st Store the details
    user.
      `perform vehicle lookup (trader acting)`(replacementVRN,
        vehicleRegistrationNumber,
        documentReferenceNumber,
        postcode
      ).`provide business details`
    confirmBusiness.`exit the service` // Exit the service

    //2nd validate the details are still stored
    user.`check tracking cookie is fresh`

    beforeYouStart.`go to BeforeYouStart page`.
      `is displayed`
    beforeYouStart.`click 'Start now' button`
    vehicleLookup.`is displayed`
    user.`perform vehicle lookup (trader acting)`(replacementVRN,
      vehicleRegistrationNumber,
      documentReferenceNumber,
      postcode
    )
  }

  @Then("^the confirm business details page is displayed$")
  def `the_confirm_business_details_page_is_displayed`() {
    confirmBusiness.`is displayed`
  }

  @When("^I have successfully assigned a reg mark as a private customer$")
  def `I have successfully assigned a reg mark as a private customer`() {
    vehicleLookup.`happy path for keeper`()
    captureCertificateDetails.`happy path`
    confirm.`happy path`
    success.`is displayed`
  }

  @When("^I have successfully assigned a reg mark as a business$")
  def `I have successfully assigned a reg mark as a business`() {
    vehicleLookup.`happy path for business`()
    setupBusinessDetails.`happy path`
    confirmBusiness.`happy path`
    captureCertificateDetails.`happy path`
    confirm.`happy path`
    success.`is displayed`
  }

  @Then("^the success page will contain a link to download the e-V948 pdf$")
  def `the success page will contain a link to download the e-V948 pdf`() {
    success.`is displayed`
    success.`has pdf link`
  }

  /** DO NOT REMOVE **/
  @After()
  def teardown() = webDriver.quit()
}
