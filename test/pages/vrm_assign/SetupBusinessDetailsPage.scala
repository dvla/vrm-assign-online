package pages.vrm_assign

import helpers.webbrowser.Page
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_assign.SetupBusinessDetails.BusinessContactId
import views.vrm_assign.SetupBusinessDetails.BusinessEmailId
import views.vrm_assign.SetupBusinessDetails.BusinessNameId
import views.vrm_assign.SetupBusinessDetails.BusinessPostcodeId
import views.vrm_assign.SetupBusinessDetails.SubmitId
import webserviceclients.fakes.AddressLookupServiceConstants.PostcodeInvalid
import webserviceclients.fakes.AddressLookupServiceConstants.PostcodeValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessContactValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessEmailValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessNameValid
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.{EmailId, EmailVerifyId}

object SetupBusinessDetailsPage extends Page {

  def address = s"$applicationContext/setup-business-details"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Provide your business details"

  def traderName(implicit driver: WebDriver) = textField(id(BusinessNameId))

  def traderContact(implicit driver: WebDriver) = textField(id(BusinessContactId))

  def traderEmail(implicit driver: WebDriver) = textField(id(s"${BusinessEmailId}_$EmailId"))

  def traderEmailVerify(implicit driver: WebDriver) = textField(id(s"${BusinessEmailId}_$EmailVerifyId"))

  def traderPostcode(implicit driver: WebDriver) = textField(id(BusinessPostcodeId))

  def lookup(implicit driver: WebDriver) = find(id(SubmitId)).get

  def happyPath(traderBusinessName: String = TraderBusinessNameValid,
                traderBusinessEmail: String = TraderBusinessEmailValid,
                traderBusinessPostcode: String = PostcodeValid)
               (implicit driver: WebDriver) = {
    go to SetupBusinessDetailsPage
    traderName.value = traderBusinessName
    traderEmail.value = traderBusinessEmail
    traderPostcode.value = traderBusinessPostcode
    click on lookup
  }

  def submitInvalidPostcode(implicit driver: WebDriver) = {
    go to SetupBusinessDetailsPage
    traderName.value = TraderBusinessNameValid
    traderContact.value = TraderBusinessContactValid
    traderEmail.value = TraderBusinessEmailValid
    traderPostcode.value = PostcodeInvalid
    click on lookup
  }
}
