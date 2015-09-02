package controllers

import com.google.inject.Inject
import models.CacheKeyPrefix
import models.VehicleAndKeeperLookupFormModel
import models.VrmLockedViewModel
import org.joda.time.DateTime
import play.api.mvc.Action
import play.api.mvc.Controller
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey

final class VrmLocked @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config,
                                  dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService)
                                extends Controller with DVLALogger {

  def present = Action {
    implicit request =>
      val happyPath = for {
        transactionId <- request.cookies.getString(TransactionIdCacheKey)
        bruteForcePreventionModel <- request.cookies.getModel[BruteForcePreventionModel]
        viewModel <- List(
          request.cookies.getModel[VehicleAndKeeperDetailsModel].map(m => VrmLockedViewModel(m, _: String, _: Long)),
          request.cookies.getModel[VehicleAndKeeperLookupFormModel].map(m => VrmLockedViewModel(m, _: String, _: Long))
        ).flatten.headOption
      } yield {
          logMessage(request.cookies.trackingId, Debug, "VrmLocked - Displaying the vrm locked error page")
          val timeString = bruteForcePreventionModel.dateTimeISOChronology
          val javascriptTimestamp = DateTime.parse(timeString).getMillis
          Ok(views.html.vrm_assign.vrm_locked(transactionId, viewModel(timeString, javascriptTimestamp),
            request.cookies.getModel[VehicleAndKeeperLookupFormModel]))
        }

      happyPath.getOrElse {
        logMessage(request.cookies.trackingId, Debug, "VrmLocked - Can't find cookies")
        Redirect(routes.VehicleLookup.present())
      }
  }

  def exit = Action { implicit request =>
    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }
}
