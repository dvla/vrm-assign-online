package composition.webserviceclients.vrmassignfulfil

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.vrmassignfulfil.VrmAssignFulfilService
import webserviceclients.vrmassignfulfil.VrmAssignFulfilServiceImpl

final class VrmAssignFulfilServiceBinding extends ScalaModule {

  def configure() = {
    bind[VrmAssignFulfilService].to[VrmAssignFulfilServiceImpl].asEagerSingleton()
  }
}
