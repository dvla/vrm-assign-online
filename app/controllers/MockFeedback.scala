package controllers

import com.google.inject.Inject
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import utils.helpers.Config
import views.vrm_assign.RelatedCacheKeys

final class MockFeedback @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                     config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_assign.mock_gov_uk_feedback()).
      withNewSession.
      discardingCookies(RelatedCacheKeys.RetainSet)
  }
}