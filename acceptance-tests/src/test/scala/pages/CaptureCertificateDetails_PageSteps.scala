package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.CaptureCertificateDetailsPage._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateDateValid
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateDocumentCountValid
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateTimeValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid

final class CaptureCertificateDetails_PageSteps(implicit webDriver: WebBrowserDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `happy path` = {
    `is displayed`.
      `enter certificate details`(box1 = CertificateDocumentCountValid, box2 = CertificateDateValid, box3 = CertificateTimeValid, box4 = RegistrationNumberValid).
      `enter registration number`(RegistrationNumberValid).
      `submit details`
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
    this
  }

  def `enter certificate details`(box1: String, box2: String, box3: String, box4: String) = {
    documentCount.value = box1
    date.value = box2
    time.value = box3
    registrationMark.value = box4
    this
  }

  def `enter registration number`(registrationNumber: String) = {
    prVrm.value = registrationNumber
    this
  }

  def `submit details` = {
    click on lookup
    this
  }

  def `form is filled with the values I previously entered`() = {
    documentCount.value should equal(CertificateDocumentCountValid)
    date.value should equal(CertificateDateValid)
    time.value should equal(CertificateTimeValid)
    registrationMark.value should equal(RegistrationNumberValid)
    prVrm.value should equal(RegistrationNumberValid)
  }

  def `form is not filled`() = {
    documentCount.value should equal("")
    date.value should equal("")
    time.value should equal("")
    registrationMark.value should equal("")
    prVrm.value should equal("")
  }
}
