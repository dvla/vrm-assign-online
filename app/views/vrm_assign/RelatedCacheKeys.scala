package views.vrm_assign

import models.CacheKeyPrefix
import models.IdentifierCacheKey
import play.api.http.HeaderNames.REFERER
import play.api.mvc.Request
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.bruteForcePreventionViewModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.MicroserviceResponseModel.MsResponseCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel.vehicleAndKeeperLookupDetailsCacheKey
import views.vrm_assign.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_assign.CaptureCertificateDetails.CaptureCertificateDetailsCacheKey
import views.vrm_assign.CaptureCertificateDetails.CaptureCertificateDetailsFormModelCacheKey
import views.vrm_assign.Confirm.ConfirmCacheKey
import views.vrm_assign.Confirm.GranteeConsentCacheKey
import views.vrm_assign.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_assign.Fulfil.FulfilCacheKey
import views.vrm_assign.Payment.PaymentDetailsCacheKey
import views.vrm_assign.Payment.PaymentTransNoCacheKey
import views.vrm_assign.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import views.vrm_assign.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey

object RelatedCacheKeys {

  final val SeenCookieMessageKey = "seen_cookie_message"

  val AssignSet = Set(
    bruteForcePreventionViewModelCacheKey,
    vehicleAndKeeperLookupDetailsCacheKey,
    MsResponseCacheKey,
    VehicleAndKeeperLookupFormModelCacheKey,
    CaptureCertificateDetailsCacheKey,
    CaptureCertificateDetailsFormModelCacheKey,
    ConfirmCacheKey,
    GranteeConsentCacheKey,
    REFERER,
    FulfilCacheKey,
    PaymentDetailsCacheKey,
    PaymentTransNoCacheKey
  )

  val VehicleAndKeeperLookupSet = Set(
    vehicleAndKeeperLookupDetailsCacheKey,
    MsResponseCacheKey,
    VehicleAndKeeperLookupFormModelCacheKey
  )

  val BusinessDetailsSet = Set(
    BusinessDetailsCacheKey,
    SetupBusinessDetailsCacheKey,
    StoreBusinessDetailsCacheKey
  )

  def removeCookiesOnExit(implicit request: Request[_], clientSideSessionFactory: ClientSideSessionFactory) = {
    val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
    RelatedCacheKeys.AssignSet ++ {
      if (storeBusinessDetails) Set.empty else RelatedCacheKeys.BusinessDetailsSet
    } + IdentifierCacheKey
  }
}
