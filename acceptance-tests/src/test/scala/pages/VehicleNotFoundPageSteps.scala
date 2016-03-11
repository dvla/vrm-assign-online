package pages

import org.scalatest.selenium.WebBrowser.{className, cssSelector, currentUrl, Element, find, findAll, pageSource}
import pages.vrm_assign.VehicleLookupFailurePage.url
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final class VehicleNotFoundPageSteps(implicit webDriver: WebBrowserDriver)
  extends helpers.AcceptanceTestHelper {

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
    this
  }

  def `has contact information`() = {
    val element: Option[Element] = find(cssSelector(".contact-info-wrapper"))
    element match {
      case Some(e) =>
        e should be ('displayed)
        e.text should include ("Telephone")
      case None => element should be (defined)
    }
  }

  def `has no contact information`() = {
    find(cssSelector(".contact-info-wrapper")) should be (None)
  }

  def `displays the formatted registration numbers`() = {
    val regNumbers: Iterator[Element] = findAll(className("reg-number"))
    val displayedReg = regNumbers.next.text
    val displayedVRN = regNumbers.next.text

    // Trim the result of formatVrm because Selenium trims any WebElement text.
    displayedReg should be (formatVrm(displayedReg.filter(p => !p.isSpaceChar)).trim)
    displayedVRN should be (formatVrm(displayedVRN.filter(p => !p.isSpaceChar)).trim)
  }

  def `has 'not found' message` = {
    pageSource should include("Unable to find vehicle record")
    pageSource should include("The V5C document reference number and/or the vehicle registration number entered is " +
      "either not valid or does not come from the most recent V5C issued for this vehicle.")
    pageSource should not include "This registration number cannot be assigned online"
    pageSource should not include "Download V317"
    this
  }

  def `has 'doc ref mismatch' message` = {
    pageSource should include("Unable to find vehicle record")
    pageSource should include("The V5C document reference number entered is either not valid or " +
      "does not come from the most recent V5C issued for this vehicle.")
    pageSource should not include "This registration number cannot be assigned online"
    pageSource should not include "Download V317"
    this
  }

  def `has 'postcode mismatch' message` = {
    pageSource should include("Unable to find vehicle record")
    pageSource should include("Postcode entered does not come from the most recent V5C issued for this vehicle.")
    pageSource should not include "This registration number cannot be assigned online"
    pageSource should not include "complete and submit a V317 form"
    this
  }


  def `has 'not eligible' message` = {
    pageSource should include("This registration number cannot be assigned")
    pageSource should include("Our records show this registration number cannot be put on this vehicle.")
    this
  }

  def `has 'direct to paper' message` = {
    pageSource should include("This registration number cannot be assigned online")
    pageSource should include("V750 or V778")
    this
  }
}
