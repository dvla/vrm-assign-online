package pages.vrm_assign

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser._
import views.vrm_assign.CaptureCertificateDetails
import CaptureCertificateDetails._
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants._

object CaptureCertificateDetailsPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/capture-certificate-details"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Provide your certificate details"

  def documentCount(implicit driver: WebDriver): TextField = textField(id(CertificateDocumentCountId))
  def date(implicit driver: WebDriver): TextField = textField(id(CertificateDateId))
  def time(implicit driver: WebDriver): TextField = textField(id(CertificateTimeId))
  def registrationMark(implicit driver: WebDriver): TextField = textField(id(CertificateRegistrationMarkId))
  def prVrm(implicit driver: WebDriver): TextField = textField(id(PrVrmId))

  def lookup(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(certificateDocumentCount: String = CertificateDocumentCountValid,
                certificateDate: String = CertificateDateValid,
                certificateTime: String = CertificateTimeValid,
                certificateRegistrationMark: String = CertificateRegistrationMarkValid,
                certificatePrVrm: String = PrVrmValid)
               (implicit driver: WebDriver) = {
    go to SetupBusinessDetailsPage
    documentCount enter certificateDocumentCount
    date enter certificateDate
    time enter certificateTime
    registrationMark enter certificateRegistrationMark
    prVrm enter certificatePrVrm
    click on lookup
  }
}