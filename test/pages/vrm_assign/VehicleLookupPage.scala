package pages.vrm_assign

import helpers.webbrowser.Page
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_assign.VehicleLookup.DocumentReferenceNumberId
import views.vrm_assign.VehicleLookup.KeeperConsentId
import views.vrm_assign.VehicleLookup.PostcodeId
import views.vrm_assign.VehicleLookup.SubmitId
import views.vrm_assign.VehicleLookup.UserType_Business
import views.vrm_assign.VehicleLookup.UserType_Keeper
import views.vrm_assign.VehicleLookup.VehicleRegistrationNumberId
import webserviceclients.fakes.BruteForcePreventionWebServiceConstants
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperPostcodeValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.ReferenceNumberValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid

object VehicleLookupPage extends Page {

  def address = s"$applicationContext/vehicle-lookup"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Getting started"

  def vehicleRegistrationNumber(implicit driver: WebDriver) = textField(id(VehicleRegistrationNumberId))

  def documentReferenceNumber(implicit driver: WebDriver) = textField(id(DocumentReferenceNumberId))

  def keeperPostcode(implicit driver: WebDriver) = textField(id(PostcodeId))

  def currentKeeperYes(implicit driver: WebDriver): RadioButton = radioButton(id(KeeperConsentId + "_" + UserType_Keeper))

  def currentKeeperNo(implicit driver: WebDriver): RadioButton = radioButton(id(KeeperConsentId + "_" + UserType_Business))

  def findVehicleDetails(implicit driver: WebDriver) = find(id(SubmitId)).get

  def fillWith(referenceNumber: String = ReferenceNumberValid,
                registrationNumber: String = RegistrationNumberValid,
                postcode: String = KeeperPostcodeValid,
                isCurrentKeeper: Boolean = true)
               (implicit driver: WebDriver) = {
    go to VehicleLookupPage
    documentReferenceNumber.value = referenceNumber
    vehicleRegistrationNumber.value = registrationNumber
    keeperPostcode.value = postcode
    if (isCurrentKeeper) click on currentKeeperYes
    else click on currentKeeperNo
    click on findVehicleDetails
  }

  def tryLockedVrm()(implicit driver: WebDriver) = {
    go to VehicleLookupPage
    documentReferenceNumber.value = ReferenceNumberValid
    vehicleRegistrationNumber.value = BruteForcePreventionWebServiceConstants.VrmLocked
    keeperPostcode.value = KeeperPostcodeValid
    click on currentKeeperYes
    click on findVehicleDetails
  }
}