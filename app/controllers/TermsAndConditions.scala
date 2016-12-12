package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.mappings.Time.fromMinutes
import utils.helpers.Config

final class TermsAndConditions @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                     config: Config,
                                     dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService)
                                     extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_assign.terms_and_conditions(fromMinutes(config.openingTimeMinOfDay),
      fromMinutes(config.closingTimeMinOfDay)
    ))
  }
}
