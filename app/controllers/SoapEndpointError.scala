package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

final class SoapEndpointError @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                          config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_assign.soap_endpoint_error())
  }
}