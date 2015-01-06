package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually._
import org.scalatest.selenium.WebBrowser._
import pages.vrm_assign.CaptureCertificateDetailsPage._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

class CaptureCertificateDetails_PageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
      pageTitle should equal(title)
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
}
