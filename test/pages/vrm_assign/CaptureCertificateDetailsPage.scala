package pages.vrm_assign

import helpers.webbrowser._
import views.vrm_assign.CaptureCertificateDetails
import CaptureCertificateDetails.{ReferenceNumberId, PrVrmId, SubmitId}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import webserviceclients.fakes.AddressLookupServiceConstants._

object CaptureCertificateDetailsPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/capture_certificate-details"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Provide your certificate details"

  def referenceNumber(implicit driver: WebDriver): TextField = textField(id(ReferenceNumberId))

  def prVrm(implicit driver: WebDriver): TextField = textField(id(PrVrmId))

  def lookup(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(certificateReferenceNumber: String = CertificateReferenceNumberValid,
                certificatePrVrm: String = CertificatePrVrmValid)
               (implicit driver: WebDriver) = {
    go to SetupBusinessDetailsPage
    referenceNumber enter certificateReferenceNumber
    prVrm enter certificatePrVrm
    click on lookup
  }
}