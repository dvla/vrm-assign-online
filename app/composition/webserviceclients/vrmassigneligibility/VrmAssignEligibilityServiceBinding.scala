package composition.webserviceclients.vrmassigneligibility

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityService
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityServiceImpl

final class VrmAssignEligibilityServiceBinding extends ScalaModule {

  def configure() = {
    bind[VrmAssignEligibilityService].to[VrmAssignEligibilityServiceImpl].asEagerSingleton()
  }
}
