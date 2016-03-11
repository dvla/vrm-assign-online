package personalizedAssignment.stepDefs

import cucumber.api.java.After
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import org.scalatest.selenium.WebBrowser.{go, goBack}
import pages.BeforeYouStartPageSteps
import pages.CaptureCertificateDetailsPageSteps
import pages.ConfirmBusinessPageSteps
import pages.ConfirmPageSteps
import pages.ConfirmPaymentPageSteps
import pages.PaymentPageSteps
import pages.PaymentPreventBackPageSteps
import pages.SetupBusinessDetailsPageSteps
import pages.SuccessPageSteps
import pages.VehicleLookupPageSteps
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.CaptureCertificateDetailsPage
import pages.vrm_assign.ConfirmBusinessPage
import pages.vrm_assign.ConfirmPage
import pages.vrm_assign.PaymentPage
import pages.vrm_assign.SetupBusinessDetailsPage
import pages.vrm_assign.SuccessPage
import pages.vrm_assign.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import views.vrm_assign.Fulfil.FulfilCacheKey
import views.vrm_assign.Payment.PaymentDetailsCacheKey
import views.vrm_assign.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.MicroserviceResponseModel.MsResponseCacheKey

final class NavigationStepDefs(implicit webDriver: WebBrowserDriver) extends helpers.AcceptanceTestHelper {

  private val beforeYouStart = new BeforeYouStartPageSteps()
  private val vehicleLookup = new VehicleLookupPageSteps()
  private val captureCertificateDetails = new CaptureCertificateDetailsPageSteps()
  private val confirm = new ConfirmPageSteps()
  private val confirmPayment = new ConfirmPaymentPageSteps()
  private val payment = new PaymentPageSteps()
  private val paymentPreventBack = new PaymentPreventBackPageSteps()
  private val setupBusinessDetails = new SetupBusinessDetailsPageSteps()
  private val confirmBusiness = new ConfirmBusinessPageSteps()
  private val success = new SuccessPageSteps()

  @Given( """^that I am on the "(.*?)" page$""")
  def `that I am on the <origin> page`(origin: String) {
    origin match {
      case "vehicle-lookup" =>
        // Starting the service takes you to this page
        vehicleLookup.`is displayed`
      case "setup-business-details" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`is displayed`
      case "business-choose-your-address" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
      case "confirm-business" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        confirmBusiness.`is displayed`
      case "capture-certificate-details (keeper acting)" =>
        vehicleLookup.`happy path for keeper`
        captureCertificateDetails.`is displayed`
      case "capture-certificate-details (business acting)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        confirmBusiness.`happy path`
        captureCertificateDetails.`is displayed`
      case "confirm" =>
        vehicleLookup.`happy path for keeper`
        captureCertificateDetails.`happy path`
        confirm.`is displayed`
      case "confirm (business acting)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        confirmBusiness.`happy path`
        captureCertificateDetails.`happy path`
        confirm.`is displayed`
      case "payment (keeper acting)" =>
        vehicleLookup.`happy path for keeper`
        captureCertificateDetails.`happy path`
        confirm.`happy path`
        confirmPayment.`happy path`
        payment.`is displayed`
      case "payment (business acting)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        confirmBusiness.`happy path`
        captureCertificateDetails.`happy path`
        confirm.`happy path`
        confirmPayment.`happy path`
        payment.`is displayed`
      case "success" => vehicleLookup.`happy path for keeper`
        captureCertificateDetails.`happy path`
        confirm.`happy path`
        confirmPayment.`happy path`
        payment.`happy path`
        success.`is displayed`
      case e => throw new RuntimeException(s"unknown 'origin' value: $e")
    }
  }

  @When( """^I enter the url for the "(.*?)" page$""")
  def `I enter the url for the <target> page`(target: String) {
    target match {
      case "before-you-start" => go to BeforeYouStartPage
      case "vehicle-lookup" => go to VehicleLookupPage
      case "setup-business-details" => go to SetupBusinessDetailsPage
      case "confirm-business" => go to ConfirmBusinessPage
      case "capture-certificate-details" => go to CaptureCertificateDetailsPage
      case "confirm" => go to ConfirmPage
      case "payment" => go to PaymentPage
      case "success" => go to SuccessPage
      case e => throw new RuntimeException(s"unknown 'target' value: $e")
    }
  }

  @When( """^I press the browser's back button$""")
  def `I press the browser's back button`() {
    goBack()
  }

  @Then( """^I am redirected to the "(.*?)" page$""")
  def `I am taken to the <expected> page`(expected: String) {
    expected match {
      case "before-you-start" => beforeYouStart.`is displayed`
      case "vehicle-lookup" => vehicleLookup.`is displayed`
      case "setup-business-details" => setupBusinessDetails.`is displayed`
      case "capture-certificate-details" => captureCertificateDetails.`is displayed`
      case "confirm-business" => confirmBusiness.`is displayed`
      case "confirm" => confirm.`is displayed`
      case "confirm-payment" => confirmPayment.`is displayed`
      case "payment" => payment.`is displayed`
      case "payment-prevent-back" => paymentPreventBack.`is displayed`
      case "success" => success.`is displayed`
      case e => throw new RuntimeException(s"unknown 'expected' value: $e")
    }
  }

  @Then("^the \"(.*?)\" form is \"(.*?)\" with the values I previously entered$")
  def `the <expected> form is <filled> with the values I previously entered`(expected: String, filled: String) {
    filled match {
      case "filled" => `the <expected> form is filled with the values I previously entered`(expected)
      case "not filled" => `the <expected> form is not filled with the values I previously entered`(expected)
      case e => throw new RuntimeException(s"unknown 'filled' value")
    }
  }

  @Then( """^the "(.*?)" form is filled with the values I previously entered$""")
  def `the <expected> form is filled with the values I previously entered`(expected: String) {
    expected match {
      case "before-you-start" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "vehicle-lookup" => vehicleLookup.`form is filled with the values I previously entered`()
      case "setup-business-details" => setupBusinessDetails.`form is filled with the values I previously entered`
      case "confirm-business" => confirmBusiness.`form is filled with the values I previously entered`()
      case "capture-certificate-details (business acting)" =>
        captureCertificateDetails.`form is filled with the values I previously entered`()
      case "capture-certificate-details" =>
        captureCertificateDetails.`form is filled with the values I previously entered`()
      case "confirm" => confirm.`form is filled with the values I previously entered`()
      case "confirm-payment" => confirmPayment.`form is filled with the values I previously entered`()
      case "confirm (business acting)" => confirm.`form is filled with the values I previously entered`()
      case "payment" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "payment-prevent-back" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "success" => ???
      case e => throw new RuntimeException(s"unknown 'expected' value")
    }
  }

  @Then( """^the "(.*?)" form is not filled with the values I previously entered$""")
  def `the <expected> form is not filled with the values I previously entered`(expected: String) {
    expected match {
      case "before-you-start" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "vehicle-lookup" => vehicleLookup.`form is not filled`()
      case "setup-business-details" => setupBusinessDetails.`form is not filled`
      case "confirm-business" => confirmBusiness.`form is not filled`()
      case "capture-certificate-details (business acting)" => captureCertificateDetails.`form is not filled`()
      case "capture-certificate-details" => captureCertificateDetails.`form is not filled`()
      case "confirm" => confirm.`form is not filled`()
      case "confirm (business acting)" => confirm.`form is not filled`()
      case "payment" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "payment-prevent-back" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "success" => ???
      case e => throw new RuntimeException(s"unknown 'expected' value")
    }
  }

  @Then( """^the payment, retain and both vehicle-and-keeper cookies are "(.*?)"$""")
  def `the cookies are <wiped>`(wiped: String) {
    wiped match {
      case "wiped" =>
        webDriver.manage().getCookieNamed(FulfilCacheKey) should equal(null)
        webDriver.manage().getCookieNamed(PaymentDetailsCacheKey) should equal(null)
        webDriver.manage().getCookieNamed(VehicleAndKeeperLookupFormModelCacheKey) should equal(null)
        webDriver.manage().getCookieNamed(MsResponseCacheKey) should equal(null)
      case "not wiped" => println("not wiped")
      case "-" => println("not created in the first place")

      case e => throw new RuntimeException(s"unknown 'wiped' value: $e")
    }
  }

  /** DO NOT REMOVE **/
  @After()
  def teardown() = webDriver.quit()
}
