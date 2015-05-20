package utils.helpers

import com.google.inject.Inject
import play.api.mvc.Cookie
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieFlags
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookie
import views.vrm_assign.BusinessChooseYourAddress.BusinessChooseYourAddressCacheKey
import views.vrm_assign.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_assign.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_assign.SetupBusinessDetails.SetupBusinessDetailsCacheKey

final class AssignCookieFlags @Inject()()(implicit val config: Config) extends CookieFlags {

  override def applyToCookie(cookie: Cookie, key: String): Cookie =
    if (List(StoreBusinessDetailsCacheKey,
      BusinessDetailsCacheKey,
      BusinessChooseYourAddressCacheKey,
      SetupBusinessDetailsCacheKey).contains(key)) {
      cookie
        .withSecure(config.secureCookies)
        .withMaxAge(config.storeBusinessDetailsMaxAge)
    } else {
      cookie
        .withSecure(config.secureCookies)
        .withMaxAge(config.cookieMaxAge)
    }

  override def applyToCookie(cookie: Cookie): Cookie = applyToCookie(cookie, key = "")
}