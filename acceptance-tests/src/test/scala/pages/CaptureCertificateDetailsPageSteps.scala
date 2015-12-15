package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser.{click, currentUrl}
import pages.vrm_assign.CaptureCertificateDetailsPage.{documentCount, date, lookup, registrationMark, time, url}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import uk.gov.dvla.vehicles.presentation.common.testhelpers.RandomVrmGenerator
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateDateValid
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateDocumentCountValid
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateTimeValid

final class CaptureCertificateDetailsPageSteps(implicit webDriver: WebBrowserDriver, timeout: PatienceConfig)
  extends ScalaDsl with EN with Matchers {

  private val RegistrationNumberValid = RandomVrmGenerator.uniqueVrm

  def `happy path` = {
    `is displayed`.
      `enter certificate details`(box1 = CertificateDocumentCountValid,
        box2 = CertificateDateValid,
        box3 = CertificateTimeValid,
        box4 = RegistrationNumberValid
      ).`submit details`
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }(timeout)
    this
  }

  def `enter certificate details`(box1: String, box2: String, box3: String, box4: String) = {
    documentCount.value = box1
    date.value = box2
    time.value = box3
    registrationMark.value = box4
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
  }

  def `form is not filled`() = {
    documentCount.value should equal("")
    date.value should equal("")
    time.value should equal("")
    registrationMark.value should equal("")
  }
}
