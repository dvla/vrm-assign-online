package helpers.vrm_assign

import models._
import org.joda.time.DateTime
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import play.api.libs.json.Json
import play.api.libs.json.Writes
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressAndPostcodeViewModel
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel
import views.vrm_assign
import views.vrm_assign.BusinessChooseYourAddress.BusinessChooseYourAddressCacheKey
import views.vrm_assign.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_assign.CaptureCertificateDetails._
import views.vrm_assign.Confirm.ConfirmCacheKey
import views.vrm_assign.Confirm.GranteeConsentCacheKey
import views.vrm_assign.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_assign.EnterAddressManually.EnterAddressManuallyCacheKey
import views.vrm_assign.Fulfil.FulfilCacheKey
import views.vrm_assign.Payment.PaymentTransNoCacheKey
import views.vrm_assign.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey
import views.vrm_assign.VehicleLookup.VehicleAndKeeperLookupResponseCodeCacheKey
import webserviceclients.fakes.AddressLookupServiceConstants._
import webserviceclients.fakes.AddressLookupWebServiceConstants.traderUprnValid
import webserviceclients.fakes.BruteForcePreventionWebServiceConstants.MaxAttempts
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants._
import webserviceclients.fakes.CaptureCertificateDetailsWebServiceConstants._
import webserviceclients.fakes.PaymentSolveWebServiceConstants._
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._
import webserviceclients.fakes.VrmAssignFulfilWebServiceConstants._

object CookieFactoryForUISpecs {

  private def addCookie[A](key: String, value: A)(implicit tjs: Writes[A], webDriver: WebDriver): Unit = {
    val valueAsString = Json.toJson(value).toString()
    val manage = webDriver.manage()
    val cookie = new Cookie(key, valueAsString)
    manage.addCookie(cookie)
  }

  def setupBusinessDetails(businessName: String = TraderBusinessNameValid,
                           businessContact: String = TraderBusinessContactValid,
                           businessEmail: String = TraderBusinessEmailValid,
                           businessPostcode: String = PostcodeValid)(implicit webDriver: WebDriver) = {
    val key = SetupBusinessDetailsCacheKey
    val value = SetupBusinessDetailsFormModel(name = businessName,
      contact = businessContact,
      email = businessEmail,
      postcode = businessPostcode)
    addCookie(key, value)
    this
  }

  def businessChooseYourAddress(uprn: Long = traderUprnValid)(implicit webDriver: WebDriver) = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressFormModel(uprnSelected = uprn.toString)
    addCookie(key, value)
    this
  }

  def enterAddressManually()(implicit webDriver: WebDriver) = {
    val key = EnterAddressManuallyCacheKey
    val value = EnterAddressManuallyModel(addressAndPostcodeViewModel = AddressAndPostcodeViewModel(
      addressLinesModel = AddressLinesViewModel(buildingNameOrNumber = BuildingNameOrNumberValid,
        line2 = Some(Line2Valid),
        line3 = Some(Line3Valid),
        postTown = PostTownValid)))
    addCookie(key, value)
    this
  }

  def businessDetails(address: AddressModel = addressWithoutUprn)(implicit webDriver: WebDriver) = {
    val key = BusinessDetailsCacheKey
    val value = BusinessDetailsModel(name = TraderBusinessNameValid,
      contact = TraderBusinessContactValid,
      email = TraderBusinessEmailValid,
      address = address)
    addCookie(key, value)
    this
  }

  def transactionId(transactionId: String = TransactionIdValid)(implicit webDriver: WebDriver) = {
    val key = TransactionIdCacheKey
    addCookie(key, transactionId)
    this
  }

  def bruteForcePreventionViewModel(permitted: Boolean = true,
                                    attempts: Int = 0,
                                    maxAttempts: Int = MaxAttempts,
                                    dateTimeISOChronology: String = org.joda.time.DateTime.now().toString)
                                   (implicit webDriver: WebDriver) = {
    val key = BruteForcePreventionViewModelCacheKey
    val value = BruteForcePreventionModel(
      permitted,
      attempts,
      maxAttempts,
      dateTimeISOChronology
    )
    addCookie(key, value)
    this
  }

  def vehicleAndKeeperLookupFormModel(referenceNumber: String = ReferenceNumberValid,
                                      registrationNumber: String = RegistrationNumberValid,
                                      postcode: String = PostcodeValid,
                                      keeperConsent: String = KeeperConsentValid)
                                     (implicit webDriver: WebDriver) = {
    val key = vrm_assign.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey
    val value = VehicleAndKeeperLookupFormModel(referenceNumber = referenceNumber,
      registrationNumber = registrationNumber, postcode = postcode, userType = keeperConsent)
    addCookie(key, value)
    this
  }

  def vehicleAndKeeperDetailsModel(registrationNumber: String = RegistrationNumberValid,
                                   vehicleMake: Option[String] = VehicleMakeValid,
                                   vehicleModel: Option[String] = VehicleModelValid,
                                   title: Option[String] = KeeperTitleValid,
                                   firstName: Option[String] = KeeperFirstNameValid,
                                   lastName: Option[String] = KeeperLastNameValid,
                                   addressLine1: Option[String] = KeeperAddressLine1Valid,
                                   addressLine2: Option[String] = KeeperAddressLine2Valid,
                                   postTown: Option[String] = KeeperPostTownValid,
                                   postCode: Option[String] = KeeperPostCodeValid)
                                  (implicit webDriver: WebDriver) = {
    val key = vrm_assign.VehicleLookup.VehicleAndKeeperLookupDetailsCacheKey
    val addressAndPostcodeModel = AddressAndPostcodeViewModel(
      addressLinesModel = AddressLinesViewModel(
        buildingNameOrNumber = addressLine1.get,
        line2 = addressLine2,
        line3 = None,
        postTown = PostTownValid
      )
    )
    val addressViewModel = AddressModel.from(addressAndPostcodeModel, postCode.get)
    val value = VehicleAndKeeperDetailsModel(registrationNumber = registrationNumber,
      make = vehicleMake,
      model = vehicleModel,
      title = title,
      firstName = firstName,
      lastName = lastName,
      address = Some(addressViewModel))
    addCookie(key, value)
    this
  }

  def vehicleAndKeeperLookupResponseCode(responseCode: String)
                                        (implicit webDriver: WebDriver) = {
    val key = VehicleAndKeeperLookupResponseCodeCacheKey
    val value = responseCode
    addCookie(key, value)
    this
  }

  def storeBusinessDetailsConsent(consent: String)(implicit webDriver: WebDriver) = {
    val key = StoreBusinessDetailsCacheKey
    addCookie(key, consent)
    this
  }

  def confirmFormModel(keeperEmail: Option[String] = KeeperEmailValid,
                       granteeConsent: String = GranteeConsentValid,
                       supplyEmail: String = "true")(implicit webDriver: WebDriver) = {
    val key = ConfirmCacheKey
    val value = ConfirmFormModel(
      keeperEmail = keeperEmail,
      granteeConsent = granteeConsent,
      supplyEmail = supplyEmail
    )
    addCookie(key, value)
    this
  }

  def granteeConsent(granteeConsent: String = GranteeConsentValid)(implicit webDriver: WebDriver) = {
    val key = GranteeConsentCacheKey
    addCookie(key, granteeConsent)
    this
  }

  def fulfilModel(transactionTimestamp: String = TransactionTimestampValid)(implicit webDriver: WebDriver) = {
    val key = FulfilCacheKey
    val value = FulfilModel(transactionTimestamp = transactionTimestamp)
    addCookie(key, value)
    this
  }

  def paymentTransNo(paymentTransNo: String = PaymentTransNoValid)(implicit webDriver: WebDriver) = {
    val key = PaymentTransNoCacheKey
    addCookie(key, paymentTransNo)
    this
  }

  def paymentModel(trxRef: Option[String] = TransactionReferenceValid,
                   paymentStatus: Option[String] = None,
                   maskedPAN: Option[String] = MaskedPANValid,
                   authCode: Option[String] = AuthCodeValid,
                   merchantId: Option[String] = MerchantIdValid,
                   paymentType: Option[String] = PaymentTypeValid,
                   cardType: Option[String] = CardTypeValid,
                   totalAmountPaid: Option[Long] = TotalAmountPaidValid,
                   rejectionCode: Option[String] = None)(implicit webDriver: WebDriver) = {
    val key = vrm_assign.Payment.PaymentDetailsCacheKey
    val value = PaymentModel(trxRef = trxRef,
      paymentStatus = paymentStatus,
      maskedPAN = maskedPAN,
      authCode = authCode,
      merchantId = merchantId,
      paymentType = paymentType,
      cardType = cardType,
      totalAmountPaid = totalAmountPaid,
      rejectionCode = rejectionCode
    )
    addCookie(key, value)
    this
  }

  def captureCertificateDetailsModel(prVrm: String = PrVrmValid, lastDate: Option[DateTime] = LastDateValid,
                                     datesList: List[String] = DatesListValid, fees: Int = FeesValid)(implicit webDriver: WebDriver) = {
    val key = CaptureCertificateDetailsCacheKey
    val value = CaptureCertificateDetailsModel(prVrm, lastDate, datesList, fees)
    addCookie(key, value)
    this
  }

  def captureCertificateDetailsFormModel(certificateDocumentCount: String = CertificateDocumentCountValid,
                                         certificateDate: String = CertificateDateValid,
                                         certificateTime: String = CertificateTimeValid,
                                         certificateRegistrationMark: String = CertificateRegistrationMarkValid,
                                         prVrm: String = PrVrmValid)(implicit webDriver: WebDriver) = {
    val key = CaptureCertificateDetailsFormModelCacheKey
    val value = CaptureCertificateDetailsFormModel(certificateDocumentCount, certificateDate, certificateTime, certificateRegistrationMark, prVrm)
    addCookie(key, value)
    this
  }
}