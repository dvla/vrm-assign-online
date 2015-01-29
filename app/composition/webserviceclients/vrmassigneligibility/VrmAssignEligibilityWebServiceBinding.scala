package composition.webserviceclients.vrmassigneligibility

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieFlags
import utils.helpers.AssignCookieFlags
import webserviceclients.vrmretentioneligibility.{VrmAssignEligibilityWebServiceImpl, VrmAssignEligibilityWebService}

final class VrmAssignEligibilityWebServiceBinding extends ScalaModule {

  def configure() = {
    bind[VrmAssignEligibilityWebService].to[VrmAssignEligibilityWebServiceImpl].asEagerSingleton()
  }
}
