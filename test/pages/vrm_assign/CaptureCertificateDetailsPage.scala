package pages.vrm_assign

import helpers.webbrowser.Page
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_assign.CaptureCertificateDetails._
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants._

object CaptureCertificateDetailsPage extends Page {

  def address = s"$applicationContext/capture-certificate-details"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Provide your certificate details"

  def documentCount(implicit driver: WebDriver) = textField(id(CertificateDocumentCountId))

  def date(implicit driver: WebDriver) = textField(id(CertificateDateId))

  def time(implicit driver: WebDriver) = textField(id(CertificateTimeId))

  def registrationMark(implicit driver: WebDriver) = textField(id(CertificateRegistrationMarkId))

  def prVrm(implicit driver: WebDriver) = textField(id(PrVrmId))

  def lookup(implicit driver: WebDriver) = find(id(SubmitId)).get

  def happyPath(certificateDocumentCount: String = CertificateDocumentCountValid,
                certificateDate: String = CertificateDateValid,
                certificateTime: String = CertificateTimeValid,
                certificateRegistrationMark: String = CertificateRegistrationMarkValid,
                certificatePrVrm: String = PrVrmValid)
               (implicit driver: WebDriver) = {
    go to SetupBusinessDetailsPage
    documentCount.value = certificateDocumentCount
    date.value = certificateDate
    time.value = certificateTime
    registrationMark.value = certificateRegistrationMark
    prVrm.value = certificatePrVrm
    click on lookup
  }
}