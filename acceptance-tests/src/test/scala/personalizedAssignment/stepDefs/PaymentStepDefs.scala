package personalizedAssignment.stepDefs

import _root_.common.CommonStepDefs
import cucumber.api.java.After
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import org.scalatest.selenium.WebBrowser.pageSource
import pages.BeforeYouStartPageSteps
import pages.CaptureCertificateDetailsPageSteps
import pages.ConfirmBusinessPageSteps
import pages.ConfirmPageSteps
import pages.ConfirmPaymentPageSteps
import pages.PaymentPageSteps
import pages.SetupBusinessDetailsPageSteps
import pages.VehicleLookupPageSteps
import pages.VrmLockedPageSteps
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class PaymentStepDefs(implicit webDriver: WebBrowserDriver) extends helpers.AcceptanceTestHelper {

  private val beforeYouStart = new BeforeYouStartPageSteps()
  private val vehicleLookup = new VehicleLookupPageSteps()
  private val captureCertificateDetails = new CaptureCertificateDetailsPageSteps()
  private val confirm = new ConfirmPageSteps()
  private val confirmPayment = new ConfirmPaymentPageSteps()
  private val payment = new PaymentPageSteps()
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

  @Given("^that I have started the PR Assign Service for payment$")
  def `that I have started the PR Assign Service for payment`() {
    user.`start the Assign service`
  }

  @Given("^I search and confirm the vehicle to be registered$")
  def `i search and confirm the vehicle to be registered`() = {
    val vrnFeesDue = "DD22"
    vehicleLookup.`happy path for keeper`(vrnFeesDue)
    captureCertificateDetails.`happy path`
    confirm.`happy path`
    confirmPayment.`happy path`
  }

  @When("^I enter payment details as \"(.*?)\",\"(.*?)\" and \"(.*?)\"$")
  def `i enter payment details as <CardName>,<CardNumber> and <SecurityCode>`(cardName: String,
                                                                              cardNumber: String,
                                                                              cardExpiry: String) = {
    payment
      .`is displayed`
      .enter(cardName, cardNumber, cardExpiry)
      .`expiryDate`
  }

  @When("^proceed to the payment$")
  def `proceed to the payment`() = {
    payment.`paynow`
    payment.`no javascript continue`
    payment.`enter password`
    payment.`no javascript submit`
    payment.`no javascript continue`
  }

  @Then("^following \"(.*?)\" should be displayed$")
  def `following should be displayed`(Message: String) = {
    eventually {
      pageSource should include(Message)
    }
  }

  /** DO NOT REMOVE **/
  @After()
  def teardown() = webDriver.quit()
}
