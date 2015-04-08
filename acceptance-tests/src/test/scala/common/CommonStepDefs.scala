package common

import composition.TestHarness
import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.selenium.WebBrowser._
import pages._
import pages.vrm_assign.ConfirmBusinessPage
import pages.vrm_assign.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class CommonStepDefs(
                            beforeYouStart: BeforeYouStartPageSteps,
                            vehicleLookup: VehicleLookupPageSteps,
                            vrmLocked: VrmLockedPageSteps,
                            captureCertificateDetails: CaptureCertificateDetailsPageSteps,
                            setupBusinessDetails: SetupBusinessDetailsPageSteps,
                            businessChooseYourAddress: BusinessChooseYourAddressPageSteps,
                            confirmBusiness: ConfirmBusinessPageSteps
                            )(implicit webDriver: WebBrowserDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers with TestHarness {

  def `start the Assign service` = {
    // IMPORTANT:: this code will not work with the accept sandbox task. Will leave it like this until I speak to Tanvi
    //    val TestUrl = "test.url"
    //    val value = s"http://localhost:9000/"
    //    Logger.debug(s"configureTestUrl - Set system property ${TestUrl} to value $value")
    //    sys.props += ((TestUrl, value))
    beforeYouStart.`go to BeforeYouStart page`.
      `is displayed`
    delete all cookies
    beforeYouStart.`click 'Start now' button`
    vehicleLookup.`is displayed`
    this
  }

  def `quit the browser` = {
    webDriver.quit()
    this
  }

  def `perform vehicle lookup (trader acting)`(registrationNumber: String, docRefNumber: String, postcode: String) = {
    vehicleLookup.
      enter(registrationNumber, docRefNumber, postcode).
      `keeper is not acting`.
      `find vehicle`
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

  def `provide business details` = {
    setupBusinessDetails.
      `is displayed`.
      `enter business details`
    businessChooseYourAddress.`choose address from the drop-down`
    confirmBusiness.`is displayed`
    click on ConfirmBusinessPage.rememberDetails
    click on ConfirmBusinessPage.confirm
    this
  }

  def `check tracking cookie is fresh` = {
    val c = cookie(TrackingIdCookieName)
    try {
      c.underlying.validate() // The java method returns void or throws, so to make it testable you should wrap it in a try-catch.
    } catch {
      case e: Throwable => fail(s"Cookie should be valid and not have thrown exception: $e")
    }
    //    val timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime)
    //    cookie("tracking_id").value should include(timeStamp) // This is not possible to test as the cookie content is encrypted and the test framework will not the decryption key.
    //    c.expiry should not be None // It is not a session cookie.
    this
  }
}
