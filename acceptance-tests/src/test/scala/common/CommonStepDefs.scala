package common

import composition.TestHarness
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.selenium.WebBrowser.{click, cookie, cookies, delete, go}
import pages.BeforeYouStartPageSteps
import pages.CaptureCertificateDetailsPageSteps
import pages.ConfirmBusinessPageSteps
import pages.SetupBusinessDetailsPageSteps
import pages.VehicleLookupPageSteps
import pages.VrmLockedPageSteps
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
                            confirmBusiness: ConfirmBusinessPageSteps
                            )(implicit webDriver: WebBrowserDriver)
  extends helpers.AcceptanceTestHelper with TestHarness {

  def `start the Assign service` = {
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

  def `perform vehicle lookup (trader acting)`(replacementVRN: String,
                                               registrationNumber: String,
                                               docRefNumber: String,
                                               postcode: String) = {
    vehicleLookup.
      enter(replacementVRN, registrationNumber, docRefNumber, postcode).
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
    captureCertificateDetails.`enter certificate details`("1", "11111", "111111", "ABC123")
    captureCertificateDetails.`submit details`
    this
  }

  def `provide business details` = {
    setupBusinessDetails.
      `is displayed`.
      `enter business details`
    confirmBusiness.`is displayed`
    click on ConfirmBusinessPage.confirm
    this
  }

  def `check tracking cookie is fresh` = {
    val c = cookie(TrackingIdCookieName)
    try {
      // The java method returns void or throws, so to make it testable you should wrap it in a try-catch.
      c.underlying.validate()
    } catch {
      case e: Throwable => fail(s"Cookie should be valid and not have thrown exception: $e")
    }
    // This is not possible to test as the cookie content is encrypted and
    // the test framework will not the decryption key.
    //    val timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime)
    //    cookie("tracking_id").value should include(timeStamp) /
    //    c.expiry should not be None // It is not a session cookie.
    this
  }
}
