package uk.gov.dvla.vehicles.assign.gatling

import io.gatling.core.Predef._
import Helper.httpConf
import Scenarios._

final class AssingSimulation extends Simulation {

  private val oneUser = atOnceUsers(1)

  setUp(
    // Happy paths
    assetsAreAccessible.inject(oneUser)
  ).
    protocols(httpConf).
    assertions(global.failedRequests.count.is(0))
}
