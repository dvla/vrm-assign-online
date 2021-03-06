package controllers

import com.google.inject.Inject
import play.api.mvc.Action
import play.api.mvc.Controller
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import utils.helpers.Config
import uk.gov.dvla.vehicles.presentation.common.mappings.Time.fromMinutes

final class MicroServiceError @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                        config: Config,
                                        dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService)
  extends Controller with DVLALogger {

  protected val tryAgainTarget = controllers.routes.VehicleLookup.present()
  protected val exitTarget = controllers.routes.BeforeYouStart.present()

  def present = Action { implicit request =>
    val trackingId = request.cookies.trackingId()
    logMessage(trackingId, Info, s"Presenting micro service error view")
    ServiceUnavailable(
      views.html.vrm_assign.micro_service_error(
        fromMinutes(config.openingTimeMinOfDay),
        fromMinutes(config.closingTimeMinOfDay),
        tryAgainTarget,
        exitTarget
      )
    )
  }
}
