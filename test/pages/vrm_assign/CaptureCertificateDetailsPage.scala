package pages.vrm_assign

import helpers.webbrowser.Page
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_assign.CaptureCertificateDetails._

object CaptureCertificateDetailsPage extends Page {

  def address = s"$applicationContext/capture-certificate-details"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Enter Certificate Details"

  def documentCount(implicit driver: WebDriver) = textField(id(CertificateDocumentCountId))

  def date(implicit driver: WebDriver) = textField(id(CertificateDateId))

  def time(implicit driver: WebDriver) = textField(id(CertificateTimeId))

  def registrationMark(implicit driver: WebDriver) = textField(id(CertificateRegistrationMarkId))

  def lookup(implicit driver: WebDriver) = find(id(SubmitId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get
}