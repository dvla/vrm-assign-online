package helpers.vrm_assign

import composition.TestComposition
import models._
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.mvc.Cookie
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.bruteForcePreventionViewModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel.vehicleAndKeeperLookupDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressAndPostcodeViewModel
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel
import views.vrm_assign.BusinessChooseYourAddress.BusinessChooseYourAddressCacheKey
import views.vrm_assign.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_assign.CaptureCertificateDetails.CaptureCertificateDetailsCacheKey
import views.vrm_assign.CaptureCertificateDetails.CaptureCertificateDetailsFormModelCacheKey
import views.vrm_assign.Confirm.ConfirmCacheKey
import views.vrm_assign.Confirm.SupplyEmail_true
import views.vrm_assign.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_assign.EnterAddressManually.EnterAddressManuallyCacheKey
import views.vrm_assign.Fulfil.FulfilCacheKey
import views.vrm_assign.Payment.PaymentDetailsCacheKey
import views.vrm_assign.Payment.PaymentTransNoCacheKey
import views.vrm_assign.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey
import views.vrm_assign.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey
import webserviceclients.fakes.AddressLookupServiceConstants._
import webserviceclients.fakes.AddressLookupWebServiceConstants.traderUprnValid
import webserviceclients.fakes.BruteForcePreventionWebServiceConstants._
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants._
import webserviceclients.fakes.CaptureCertificateDetailsWebServiceConstants._
import webserviceclients.fakes.PaymentSolveWebServiceConstants.AuthCodeValid
import webserviceclients.fakes.PaymentSolveWebServiceConstants.CardTypeValid
import webserviceclients.fakes.PaymentSolveWebServiceConstants.MaskedPANValid
import webserviceclients.fakes.PaymentSolveWebServiceConstants.MerchantIdValid
import webserviceclients.fakes.PaymentSolveWebServiceConstants.PaymentTypeValid
import webserviceclients.fakes.PaymentSolveWebServiceConstants.TotalAmountPaidValid
import webserviceclients.fakes.PaymentSolveWebServiceConstants.TransactionReferenceValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._
import webserviceclients.fakes.VrmAssignFulfilWebServiceConstants.TransactionTimestampValid

object CookieFactoryForUnitSpecs extends TestComposition {

  private final val TrackingIdValue = "trackingId"
  private lazy val session = testInjector().getInstance(classOf[ClientSideSessionFactory]).getSession(Array.empty[Cookie])

  def setupBusinessDetails(businessName: String = TraderBusinessNameValid,
                           businessContact: String = TraderBusinessContactValid,
                           businessEmail: String = TraderBusinessEmailValid,
                           businessPostcode: String = PostcodeValid): Cookie = {
    val key = SetupBusinessDetailsCacheKey
    val value = SetupBusinessDetailsFormModel(name = businessName,
      contact = businessContact,
      email = businessEmail,
      postcode = businessPostcode)
    createCookie(key, value)
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
                                   postCode: Option[String] = KeeperPostCodeValid): Cookie = {
    val key = vehicleAndKeeperLookupDetailsCacheKey
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
      address = Some(addressViewModel),
      disposeFlag = None,
      keeperEndDate = None,
      keeperChangeDate = None,
      suppressedV5Flag = None
    )
    createCookie(key, value)
  }

  private def createCookie[A](key: String, value: A)(implicit tjs: Writes[A]): Cookie = {
    val json = Json.toJson(value).toString()
    val cookieName = session.nameCookie(key)
    session.newCookie(cookieName, json)
  }

  def vehicleAndKeeperLookupFormModel(referenceNumber: String = ReferenceNumberValid,
                                      registrationNumber: String = RegistrationNumberValid,
                                      postcode: String = KeeperPostcodeValid,
                                      keeperConsent: String = KeeperConsentValid): Cookie = {
    val key = VehicleAndKeeperLookupFormModelCacheKey
    val value = VehicleAndKeeperLookupFormModel(
      referenceNumber = referenceNumber,
      registrationNumber = registrationNumber,
      postcode = postcode,
      userType = keeperConsent
    )
    createCookie(key, value)
  }

  def businessChooseYourAddressUseUprn(uprnSelected: String = traderUprnValid.toString): Cookie = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressFormModel(uprnSelected = uprnSelected)
    createCookie(key, value)
  }

  def businessChooseYourAddress(uprnSelected: String = "0"): Cookie = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressFormModel(uprnSelected = uprnSelected)
    createCookie(key, value)
  }

  def enterAddressManually(): Cookie = {
    val key = EnterAddressManuallyCacheKey
    val value = EnterAddressManuallyModel(
      addressAndPostcodeViewModel = AddressAndPostcodeViewModel(
        addressLinesModel = AddressLinesViewModel(
          buildingNameOrNumber = BuildingNameOrNumberValid,
          line2 = Some(Line2Valid),
          line3 = Some(Line3Valid),
          postTown = PostTownValid
        )
      )
    )
    createCookie(key, value)
  }

  def bruteForcePreventionViewModel(permitted: Boolean = true,
                                    attempts: Int = 0,
                                    maxAttempts: Int = MaxAttempts,
                                    dateTimeISOChronology: String = org.joda.time.DateTime.now().toString): Cookie = {
    val key = bruteForcePreventionViewModelCacheKey
    val value = BruteForcePreventionModel(
      permitted,
      attempts,
      maxAttempts,
      dateTimeISOChronology = dateTimeISOChronology
    )
    createCookie(key, value)
  }

  def trackingIdModel(value: String = TrackingIdValue): Cookie = {
    createCookie(ClientSideSessionFactory.TrackingIdCookieName, value)
  }

  private def createCookie[A](key: String, value: String): Cookie = {
    val cookieName = session.nameCookie(key)
    session.newCookie(cookieName, value)
  }

  def businessDetailsModel(businessName: String = TraderBusinessNameValid,
                           businessContact: String = TraderBusinessContactValid,
                           businessEmail: String = TraderBusinessEmailValid,
                           businessAddress: AddressModel = addressWithUprn): Cookie = {
    val key = BusinessDetailsCacheKey
    val value = BusinessDetailsModel(name = businessName,
      contact = businessContact,
      email = businessEmail,
      address = businessAddress)
    createCookie(key, value)
  }

  def confirmFormModel(keeperEmail: Option[String] = KeeperEmailValid): Cookie = {
    val key = ConfirmCacheKey
    val value = ConfirmFormModel(keeperEmail = keeperEmail, granteeConsent = "true")
    createCookie(key, value)
  }

  def transactionId(transactionId: String = TransactionIdValid): Cookie = {
    val key = TransactionIdCacheKey
    createCookie(key, transactionId)
  }

  def paymentTransNo(paymentTransNo: String = PaymentTransNoValid): Cookie = {
    val key = PaymentTransNoCacheKey
    createCookie(key, paymentTransNo)
  }

  def storeBusinessDetailsConsent(consent: String = "true"): Cookie = {
    val key = StoreBusinessDetailsCacheKey
    createCookie(key, consent)
  }

  def captureCertificateDetailsModel(prVrm: String = RegistrationNumberValid, lastDate: Option[DateTime] = LastDateValid,
                                     datesList: List[String] = DatesListValid, fees: Int = FeesValid): Cookie = {
    val key = CaptureCertificateDetailsCacheKey
    val value = CaptureCertificateDetailsModel(prVrm, lastDate, datesList, fees)
    createCookie(key, value)
  }

  def captureCertificateDetailsFormModel(certificateDocumentCount: String = CertificateDocumentCountValid,
                                         certificateDate: String = CertificateDateValid,
                                         certificateTime: String = CertificateTimeValid,
                                         certificateRegistrationMark: String = RegistrationNumberValid,
                                         prVrm: String = RegistrationNumberValid): Cookie = {
    val key = CaptureCertificateDetailsFormModelCacheKey
    val value = CaptureCertificateDetailsFormModel(certificateDocumentCount, certificateDate, certificateTime, certificateRegistrationMark, prVrm)
    createCookie(key, value)
  }

  def paymentModel(trxRef: Option[String] = TransactionReferenceValid,
                   paymentStatus: Option[String] = None,
                   maskedPAN: Option[String] = MaskedPANValid,
                   authCode: Option[String] = AuthCodeValid,
                   merchantId: Option[String] = MerchantIdValid,
                   paymentType: Option[String] = PaymentTypeValid,
                   cardType: Option[String] = CardTypeValid,
                   totalAmountPaid: Option[Long] = TotalAmountPaidValid,
                   rejectionCode: Option[String] = None,
                   isPrimaryUrl: Boolean = true): Cookie = {
    val key = PaymentDetailsCacheKey
    val value = PaymentModel(trxRef = trxRef,
      paymentStatus = paymentStatus,
      maskedPAN = maskedPAN,
      authCode = authCode,
      merchantId = merchantId,
      paymentType = paymentType,
      cardType = cardType,
      totalAmountPaid = totalAmountPaid,
      rejectionCode = rejectionCode,
      isPrimaryUrl = isPrimaryUrl
    )
    createCookie(key, value)
  }

  def fulfilModel(transactionTimestamp: String = TransactionTimestampValid): Cookie = {
    val key = FulfilCacheKey
    val value = FulfilModel(transactionTimestamp)
    createCookie(key, value)
  }
}