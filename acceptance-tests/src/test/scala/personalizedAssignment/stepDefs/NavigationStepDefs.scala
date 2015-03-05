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
import pages.BeforeYouStart_PageSteps
import pages.BusinessChooseYourAddress_PageSteps
import pages.CaptureCertificateDetails_PageSteps
import pages.Confirm_PageSteps
import pages.Payment_PageSteps
import pages.SetupBusinessDetails_PageSteps
import pages.VehicleLookup_PageSteps
import pages.VehicleNotFound_PageSteps
import pages.VrmLocked_PageSteps
import pages.vrm_assign.ConfirmPage
import pages.vrm_assign.PaymentPage
import pages.vrm_assign.SuccessPage
import pages.vrm_assign.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

import scala.concurrent.duration.DurationInt

final class NavigationStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  implicit val timeout = PatienceConfig(timeout = 30.seconds)
//  implicit val timeout = PatienceConfig(timeout = 5.seconds)
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

  @Given("""^that I am on the "(.*?)" page$""")
  def `that I am on the <origin> page`(origin: String) {
    origin match {
      case "vehicle-lookup" =>
        println("origin vehicle-lookup")
        user.`start the Assign service`
        vehicleLookup.`happy path`
      case "confirm" => println("origin confirm")
      case "payment" => println("origin payment")
      case "success" => println("origin success")
      case e => throw new RuntimeException(s"unknown 'origin' value: $e")
    }
  }

  @When("""^I enter the url for the "(.*?)" page$""")
  def `I enter the url for the <target> page`(target: String) {
    target match {
      case "vehicle-lookup" =>
        println("target vehicle-lookup")
        go to VehicleLookupPage
      case "confirm" =>
        println("target confirm")
        go to ConfirmPage
      case "payment" =>
        println("target payment")
        go to PaymentPage
      case "success" =>
        println("target success")
        go to SuccessPage
      case e => throw new RuntimeException(s"unknown 'target' value: $e")
    }
  }

  @Then("""^I am redirected to the "(.*?)" page$""")
  def `I am taken to the <expected> page`(expected: String) {
    expected match {
      case "vehicle-lookup" =>
        println("expected vehicle-lookup")
        vehicleLookup.`is displayed`
      case "confirm" => println("expected confirm")
      case "payment" => println("expected payment")
      case "success" => println("expected success")
      case e => throw new RuntimeException(s"unknown 'expected' value: $e")
    }
  }

  @Then("""^the "(.*?)" form is "(.*?)" with the values I previously entered$""")
  def `the <expected> form is <filled> with the values I previously entered`(expected: String, filled: String) {
    expected match {
      case "vehicle-lookup" =>
        println("expected vehicle-lookup is " + filled)
        filled match {
          case "filled" => vehicleLookup.`form is filled with the values I previously entered`()
          case "not filled" => ???
          case "-" => // no check
          case e => throw new RuntimeException(s"unknown 'filled' value: $e")
        }
      case "confirm" => println("expected confirm is " + filled)
      case "payment" => println("expected payment is " + filled)
      case "success" => println("expected success is " + filled)
      case e => throw new RuntimeException(s"unknown 'expected' value: $e")
    }
  }

  @Then("""^the payment, retain and both vehicle-and-keeper cookies are "(.*?)"$""")
  def `the cookies are <wiped>`(wiped: String) {
    wiped match {
      case "wiped" => println("wiped")
      case "not wiped" =>
        println("not wiped")

      case e => throw new RuntimeException(s"unknown 'wiped' value: $e")
    }
  }

  /** DO NOT REMOVE **/
  @After()
  def teardown() = webDriver.quit()
}
