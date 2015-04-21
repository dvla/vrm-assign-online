package composition.webserviceclients.vrmassigneligibility

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.vrmretentioneligibility.VrmAssignEligibilityService
import webserviceclients.vrmretentioneligibility.VrmAssignEligibilityServiceImpl

final class VrmAssignEligibilityServiceBinding extends ScalaModule {

  def configure() = {
    bind[VrmAssignEligibilityService].to[VrmAssignEligibilityServiceImpl].asEagerSingleton()
  }
}
