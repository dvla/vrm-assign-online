package controllers

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.controllers
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.config.OrdnanceSurveyConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.config.VehicleAndKeeperLookupConfig
import utils.helpers.Config

class Version @Inject()(vehiclesKeeperConfig: VehicleAndKeeperLookupConfig,
                        osAddressLookupConfig: OrdnanceSurveyConfig,
                        config2: Config)
  extends controllers.Version(
    osAddressLookupConfig.baseUrl + "/version",
    vehiclesKeeperConfig.vehicleAndKeeperLookupMicroServiceBaseUrl + "/version",
    config2.emailServiceMicroServiceUrlBase + "/version",
//    config2.paymentSolveMicroServiceUrlBase + "/version",
    config2.vrmAssignEligibilityMicroServiceUrlBase + "/version",
    config2.vrmAssignFulfilMicroServiceUrlBase + "/version"
  )
