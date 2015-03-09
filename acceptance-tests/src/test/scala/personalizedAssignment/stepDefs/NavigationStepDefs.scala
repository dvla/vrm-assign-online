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
import org.scalatest.selenium.WebBrowser._
import pages._
import pages.vrm_assign._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

import scala.concurrent.duration.DurationInt

final class NavigationStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  lazy val beforeYouStart = new BeforeYouStart_PageSteps()(webDriver, timeout)
  lazy val vehicleLookup = new VehicleLookup_PageSteps()(webDriver, timeout)
  lazy val captureCertificateDetails = new CaptureCertificateDetails_PageSteps()(webDriver, timeout)
  lazy val confirm = new Confirm_PageSteps()(webDriver, timeout)
  lazy val payment = new Payment_PageSteps()(webDriver, timeout)
  lazy val paymentPreventBack = new PaymentPreventBack_PageSteps()(webDriver, timeout)
  lazy val vehicleNotFound = new VehicleNotFound_PageSteps()(webDriver, timeout)
  lazy val vrmLocked = new VrmLocked_PageSteps()(webDriver, timeout)
  lazy val setupBusinessDetails = new SetupBusinessDetails_PageSteps()(webDriver, timeout)
  lazy val businessChooseYourAddress = new BusinessChooseYourAddress_PageSteps()(webDriver, timeout)
  lazy val confirmBusiness = new ConfirmBusiness_PageSteps()(webDriver, timeout)
  lazy val success = new Success_PageSteps()(webDriver, timeout)
  lazy val enterAddressManually = new EnterAddressManually_PageSteps()(webDriver, timeout)
  lazy val user = new CommonStepDefs(
    beforeYouStart,
    vehicleLookup,
    vrmLocked,
    captureCertificateDetails,
    setupBusinessDetails,
    businessChooseYourAddress
  )(webDriver, timeout)
  implicit val timeout = PatienceConfig(timeout = 5.seconds)

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
      case "enter-address-manually" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`click manual address entry`
        enterAddressManually.`is displayed`
      case "confirm-business" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`choose address from the drop-down`
        confirmBusiness.`is displayed`
      case "confirm-business (entered address manually)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`click manual address entry`
        enterAddressManually.`happy path`
        confirmBusiness.`is displayed`
      case "capture-certificate-details (keeper acting)" =>
        vehicleLookup.`happy path for keeper`
        captureCertificateDetails.`is displayed`
      case "capture-certificate-details (business acting)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`choose address from the drop-down`
        confirmBusiness.`happy path`
        captureCertificateDetails.`is displayed`
      case "capture-certificate-details (business acting) (entered address manually)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`click manual address entry`
        enterAddressManually.`happy path`
        confirmBusiness.`happy path`
        captureCertificateDetails.`is displayed`
      case "confirm" =>
        vehicleLookup.`happy path for keeper`
        captureCertificateDetails.`happy path`
        confirm.`is displayed`
      case "confirm (business acting)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`choose address from the drop-down`
        confirmBusiness.`happy path`
        captureCertificateDetails.`happy path`
        confirm.`is displayed`
      case "confirm (business acting) (entered address manually)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`click manual address entry`
        enterAddressManually.`happy path`
        confirmBusiness.`happy path`
        captureCertificateDetails.`happy path`
        confirm.`is displayed`
      case "payment (keeper acting)" =>
        vehicleLookup.`happy path for keeper`
        captureCertificateDetails.`happy path`
        confirm.`happy path`
        payment.`is displayed`
      case "payment (business acting)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`choose address from the drop-down`
        confirmBusiness.`happy path`
        captureCertificateDetails.`happy path`
        confirm.`happy path`
        payment.`is displayed`
      case "payment (business acting) (entered address manually)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`click manual address entry`
        enterAddressManually.`happy path`
        confirmBusiness.`happy path`
        captureCertificateDetails.`happy path`
        confirm.`happy path`
        payment.`is displayed`
      case "success" => vehicleLookup.`happy path for keeper`
        captureCertificateDetails.`happy path`
        confirm.`happy path`
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
      case "business-choose-your-address" => go to BusinessChooseYourAddressPage
      case "enter-address-manually" => go to EnterAddressManuallyPage
      case "confirm-business" => go to ConfirmBusinessPage
      case "capture-certificate-details" => go to CaptureCertificateDetailsPage
      case "capture-certificate-details (business acting)" => go to CaptureCertificateDetailsPage
      case "confirm" => go to ConfirmPage
      case "confirm (business acting)" => go to ConfirmPage
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
      case "business-choose-your-address" => businessChooseYourAddress.`is displayed`
      case "enter-address-manually" => enterAddressManually.`is displayed`
      case "capture-certificate-details" => captureCertificateDetails.`is displayed`
      case "capture-certificate-details (business acting)" => captureCertificateDetails.`is displayed`
      case "confirm-business" => confirmBusiness.`is displayed`
      case "confirm" => confirm.`is displayed`
      case "confirm (business acting)" => confirm.`is displayed`
      case "payment" => payment.`is displayed`
      case "payment-prevent-back" => paymentPreventBack.`is displayed`
      case "success" => success.`is displayed`
      case e => throw new RuntimeException(s"unknown 'expected' value: $e")
    }
  }

  @Then( """^the "(.*?)" form is "(.*?)" with the values I previously entered$""")
  def `the <expected> form is <filled> with the values I previously entered`(expected: String, filled: String) {
    expected match {
      case "before-you-start" =>
        filled match {
          case "-" => // no check as no fields on page
          case e => throw new RuntimeException(s"unknown 'filled' value: $e")
        }
      case "vehicle-lookup" =>
        filled match {
          case "filled" => vehicleLookup.`form is filled with the values I previously entered`()
          case "not filled" => vehicleLookup.`form is not filled`()
          case e => throw new RuntimeException(s"unknown 'filled' value: $e")
        }
      case "setup-business-details" =>
        filled match {
          case "filled" => setupBusinessDetails.`form is filled with the values I previously entered`
          case "not filled" => setupBusinessDetails.`form is not filled`
          case e => throw new RuntimeException(s"unknown 'filled' value: $e")
        }
      case "business-choose-your-address" =>
        filled match {
          case "filled" => businessChooseYourAddress.`form is filled with the values I previously entered`
          case "not filled" => businessChooseYourAddress.`form is not filled`
          case e => throw new RuntimeException(s"unknown 'filled' value: $e")
        }
      case "enter-address-manually" =>
        filled match {
          case "filled" => enterAddressManually.`form is filled with the values I previously entered`
          case "not filled" => enterAddressManually.`form is not filled`
          case e => throw new RuntimeException(s"unknown 'filled' value: $e")
        }
      case "confirm-business" =>
        filled match {
          case "filled" => confirmBusiness.`form is filled with the values I previously entered`()
          case "not filled" => confirmBusiness.`form is not filled`()
          case e => throw new RuntimeException(s"unknown 'filled' value: $e")
        }
      case "capture-certificate-details (business acting)" =>
        filled match {
          case "filled" => captureCertificateDetails.`form is filled with the values I previously entered`()
          case "not filled" => captureCertificateDetails.`form is not filled`()
          case e => throw new RuntimeException(s"unknown 'filled' value: $e")
        }
      case "capture-certificate-details" =>
        filled match {
          case "filled" => captureCertificateDetails.`form is filled with the values I previously entered`()
          case "not filled" => captureCertificateDetails.`form is not filled`()
          case e => throw new RuntimeException(s"unknown 'filled' value: $e")
        }
      case "confirm" =>
        filled match {
          case "filled" => confirm.`form is filled with the values I previously entered`()
          case "not filled" => confirm.`form is not filled`()
          case e => throw new RuntimeException(s"unknown 'filled' value: $e")
        }
      case "confirm (business acting)" =>
        filled match {
          case "filled" => confirm.`form is filled with the values I previously entered`()
          case "not filled" => confirm.`form is not filled`()
          case e => throw new RuntimeException(s"unknown 'filled' value: $e")
        }
      case "payment" =>
        filled match {
          case "-" => // no check as no fields on page
          case e => throw new RuntimeException(s"unknown 'filled' value: $e")
        }
      case "payment-prevent-back" =>
        filled match {
          case "-" => // no check as no fields on page
          case e => throw new RuntimeException(s"unknown 'filled' value: $e")
        }
      case "success" => ???
      case e => throw new RuntimeException(s"unknown 'expected' value: $e")
    }
  }

  @Then( """^the payment, retain and both vehicle-and-keeper cookies are "(.*?)"$""")
  def `the cookies are <wiped>`(wiped: String) {
    wiped match {
      case "wiped" =>
        import views.vrm_assign.Payment.PaymentDetailsCacheKey
        import views.vrm_assign.Fulfil.FulfilCacheKey
        import views.vrm_assign.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey
        import views.vrm_assign.VehicleLookup.VehicleAndKeeperLookupResponseCodeCacheKey
        webDriver.manage().getCookieNamed(PaymentDetailsCacheKey) should equal(null)
        webDriver.manage().getCookieNamed(VehicleAndKeeperLookupFormModelCacheKey) should equal(null)
        webDriver.manage().getCookieNamed(VehicleAndKeeperLookupResponseCodeCacheKey) should equal(null)
      case "not wiped" => println("not wiped")
      case "-" => println("not created in the first place")

      case e => throw new RuntimeException(s"unknown 'wiped' value: $e")
    }
  }

  /** DO NOT REMOVE **/
  @After()
  def teardown() = webDriver.quit()
}
