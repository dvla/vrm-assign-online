package uk.gov.dvla.vehicles.assign.gatling

import io.gatling.core.Predef.{atOnceUsers, global, Simulation}
import uk.gov.dvla.vehicles.assign.gatling.Helper.httpConf
import uk.gov.dvla.vehicles.assign.gatling.Scenarios.assetsAreAccessible

final class AssignSimulation extends Simulation {

  private val oneUser = atOnceUsers(1)

  setUp(
    // Happy paths
    assetsAreAccessible.inject(oneUser)
  ).
    protocols(httpConf).
    assertions(global.failedRequests.count.is(0))
}
