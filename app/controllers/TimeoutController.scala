package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import models.{VehicleAndKeeperLookupFormModel, CacheKeyPrefix}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_assign.Confirm.GranteeConsentCacheKey

/**
 * Shows a timeout page to inform the end user when the fulfil service timeouts.
 */
class TimeoutController @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config,
                                    dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService
                                    ) extends Controller {

  /**
   * presents the end use with the timeout page.
   * We check that the user went through the confirm page at least before we show this page.
   * @return the timeout view or a redirect to the start in the case of error.
   */
  def present = Action { implicit request =>
    (for {
      _  <- request.cookies.getString(GranteeConsentCacheKey)
    } yield {

      val vehicleDetails = request.cookies.getModel[VehicleAndKeeperDetailsModel]
      val vehicleAndKeeperForm = request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      Ok(views.html.vrm_assign.timeout(vehicleDetails, vehicleAndKeeperForm.map(v => v.replacementVRN)))

    }) getOrElse Redirect(routes.VehicleLookup.present())
  }

  def exit = Action { implicit request =>
    Redirect(routes.LeaveFeedback.present())
  }
}
