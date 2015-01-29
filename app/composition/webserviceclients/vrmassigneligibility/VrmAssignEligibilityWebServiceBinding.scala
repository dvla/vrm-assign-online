package composition.webserviceclients.vrmassigneligibility

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.vrmretentioneligibility.{VrmAssignEligibilityWebService, VrmAssignEligibilityWebServiceImpl}

final class VrmAssignEligibilityWebServiceBinding extends ScalaModule {

  def configure() = {
    bind[VrmAssignEligibilityWebService].to[VrmAssignEligibilityWebServiceImpl].asEagerSingleton()
  }
}
