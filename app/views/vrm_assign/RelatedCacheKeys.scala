package views.vrm_assign

import play.api.http.HeaderNames.REFERER
import play.api.mvc.Request
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import views.vrm_assign.BusinessChooseYourAddress.BusinessChooseYourAddressCacheKey
import views.vrm_assign.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_assign.CaptureCertificateDetails.{CaptureCertificateDetailsCacheKey, CaptureCertificateDetailsFormModelCacheKey}
import views.vrm_assign.Confirm.KeeperEmailCacheKey
import views.vrm_assign.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_assign.EnterAddressManually.EnterAddressManuallyCacheKey
import views.vrm_assign.Fulfil.{FulfilCacheKey, FulfilResponseCodeCacheKey}
import views.vrm_assign.Payment.{PaymentDetailsCacheKey, PaymentTransNoCacheKey}
import views.vrm_assign.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import views.vrm_assign.VehicleLookup.{VehicleAndKeeperLookupDetailsCacheKey, VehicleAndKeeperLookupFormModelCacheKey, VehicleAndKeeperLookupResponseCodeCacheKey}

object RelatedCacheKeys {

  final val SeenCookieMessageKey = "seen_cookie_message"

  val AssignSet = Set(
    BruteForcePreventionViewModelCacheKey,
    VehicleAndKeeperLookupDetailsCacheKey,
    VehicleAndKeeperLookupResponseCodeCacheKey,
    VehicleAndKeeperLookupFormModelCacheKey,
    CaptureCertificateDetailsCacheKey,
    CaptureCertificateDetailsFormModelCacheKey,
    EnterAddressManuallyCacheKey,
    KeeperEmailCacheKey,
    REFERER,
    FulfilCacheKey,
    FulfilResponseCodeCacheKey,
    PaymentDetailsCacheKey,
    PaymentTransNoCacheKey
  )

  val VehicleAndKeeperLookupSet = Set(
    VehicleAndKeeperLookupDetailsCacheKey,
    VehicleAndKeeperLookupResponseCodeCacheKey,
    VehicleAndKeeperLookupFormModelCacheKey
  )

  val BusinessDetailsSet = Set(
    BusinessChooseYourAddressCacheKey,
    BusinessDetailsCacheKey,
    SetupBusinessDetailsCacheKey,
    StoreBusinessDetailsCacheKey
  )

  def removeCookiesOnExit(implicit request: Request[_], clientSideSessionFactory: ClientSideSessionFactory) = {
    val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
    RelatedCacheKeys.AssignSet ++ {
      if (storeBusinessDetails) Set.empty else RelatedCacheKeys.BusinessDetailsSet
    }
  }
}