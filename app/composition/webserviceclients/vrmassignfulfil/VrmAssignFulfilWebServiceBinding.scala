package composition.webserviceclients.vrmassignfulfil

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.vrmassignfulfil.{VrmAssignFulfilWebService, VrmAssignFulfilWebServiceImpl}

final class VrmAssignFulfilWebServiceBinding extends ScalaModule {

  def configure() = {
    bind[VrmAssignFulfilWebService].to[VrmAssignFulfilWebServiceImpl].asEagerSingleton()
  }
}
