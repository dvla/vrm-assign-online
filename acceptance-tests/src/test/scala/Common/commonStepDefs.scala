package common

import composition.TestHarness
import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import org.scalatest.selenium.WebBrowser._
import pages._
import pages.vrm_assign.{ConfirmBusinessPage, VehicleLookupPage}
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

class commonStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers with TestHarness {

  lazy val beforeYouStart = new BeforeYouStart_PageSteps
  lazy val vehicleLookup = new VehicleLookup_PageSteps
  lazy val vrmLocked = new VrmLocked_PageSteps
  lazy val captureCertificateDetails = new CaptureCertificateDetails_PageSteps
  lazy val setupBusinessDetails = new SetupBusinessDetails_PageSteps
  lazy val businessChooseYourAddress = new BusinessChooseYourAddress_PageSteps

  def `start the Assign service` = {
// IMPORTANT:: this code will not work with the accept sandbox task. Will leave it like this until I speak to Tanvi
//    val TestUrl = "test.url"
//    val value = s"http://localhost:9000/"
//    Logger.debug(s"configureTestUrl - Set system property ${TestUrl} to value $value")
//    sys.props += ((TestUrl, value))
  }

  def `before you start` = {
    beforeYouStart.`go to BeforeYouStart page`.
      `is displayed`.
      `click 'Start now' button`
    vehicleLookup.`is displayed`
    this
  }

  def `quit the browser` = {
    webDriver.quit()
  }

  def `vehicleLookupDoesNotMatchRecord`(registrationNumber: String, docRefNumber: String, postcode: String) = {
    vehicleLookup.
      enter(registrationNumber, docRefNumber, postcode).
      `keeper is acting`.
      `find vehicle`
    vrmLocked.`is displayed`
    this
  }

  def `goToVehicleLookupPage` = {
    go to VehicleLookupPage
    this
  }

  def `enterCertificateDetails` = {
    captureCertificateDetails.`is displayed`
    captureCertificateDetails.`enter certificate details`("1", "11111", "111111", "11111111")
    captureCertificateDetails.`enter registration number`("AT1")
    captureCertificateDetails.`submit details`
    this
  }
  def goToVehicleLookupPageWithNonKeeper(RegistrationNumber: String, DocRefNumber: String, Postcode: String) = {
    vehicleLookup.
      enter(RegistrationNumber, DocRefNumber, Postcode).
      `keeper is not acting`.
      `find vehicle`
    //confirmBusiness.`is displayed`
    this
  }
  def provideBusinessDetails = {
    setupBusinessDetails.`is displayed`
    setupBusinessDetails.`enter business details`
    this
  }

  def chooseBusinessAddress = {
    businessChooseYourAddress.`proceed to next page`
    this
  }

  def storeBusinessDetails = {
    click on ConfirmBusinessPage.rememberDetails
    click on ConfirmBusinessPage.confirm
    this
  }

  def confirmBusinessDetailsIsDisplayed = {
    pageTitle should equal(ConfirmBusinessPage.title)
    this
  }

  def exitBusiness = {
    click on ConfirmBusinessPage.exit
    this
  }

  def validateCookieIsFresh = {
    val c = cookie(TrackingIdCookieName)
    try {
      c.underlying.validate() // The java method returns void or throws, so to make it testable you should wrap it in a try-catch.
    } catch {
      case e: Throwable => fail(s"Cookie should be valid and not have thrown exception: $e")
    }
    //    val timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime)
    //    cookie("tracking_id").value should include(timeStamp) // This is not possible to test as the cookie content is encrypted and the test framework will not the decryption key.
    c.expiry should be(None)
    this
  }


}
