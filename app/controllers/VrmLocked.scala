package controllers

import com.google.inject.Inject
import models.CacheKeyPrefix
import models.VehicleAndKeeperLookupFormModel
import models.VrmLockedViewModel
import org.joda.time.DateTime
import play.api.mvc.{Action, Request, Result}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.controllers.VrmLockedBase
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey

final class VrmLocked @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config,
                                  dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService
                                  ) extends VrmLockedBase {

  protected override def presentResult(model: BruteForcePreventionModel)(implicit request: Request[_]): Result = {
    val happyPath: Option[Result] = for {
      transactionId <- request.cookies.getString(TransactionIdCacheKey)
      vehicleAndKeeperLookupFormModel <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
    } yield {
      logMessage(request.cookies.trackingId, Debug, "VrmLocked - Displaying the vrm locked error page")
      val timeString = model.dateTimeISOChronology
      val javascriptTimestamp = DateTime.parse(timeString).getMillis
      Ok(views.html.vrm_assign.vrm_locked(
        transactionId,
        VrmLockedViewModel(vehicleAndKeeperLookupFormModel, timeString, javascriptTimestamp)
      ))
    }

    happyPath.getOrElse {
      logMessage(request.cookies.trackingId, Debug, "VrmLocked - Can't find cookies")
      Redirect(routes.VehicleLookup.present())
    }
  }

  protected override def missingBruteForcePreventionCookie(implicit request: Request[_]): Result = {
    logMessage(request.cookies.trackingId(), Debug,
      s"Missing BruceForcePreventionCookie. Redirecting to ${routes.VehicleLookup.present()}")
    Redirect(routes.VehicleLookup.present())
  }

  protected override def exitResult(implicit request: Request[_]): Result = {
    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }

  // Not used for Assign
  protected override def tryAnotherResult(implicit request: Request[_]): Result = NotFound
}
