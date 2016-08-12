package controllers

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.controllers
import uk.gov.dvla.vehicles.presentation.common.controllers.Version.Suffix
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.OrdnanceSurveyConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupConfig
import utils.helpers.Config

class Version @Inject()(vehicleAndKeeperConfig: VehicleAndKeeperLookupConfig,
                        osAddressLookupConfig: OrdnanceSurveyConfig,
                        config: Config) extends controllers.Version(
    config.auditMicroServiceUrlBase + Suffix,
    config.emailServiceMicroServiceUrlBase + Suffix,
    osAddressLookupConfig.baseUrl + Suffix,
    config.paymentSolveMicroServiceUrlBase + Suffix,
    vehicleAndKeeperConfig.vehicleAndKeeperLookupMicroServiceBaseUrl + Suffix,
    config.vrmAssignEligibilityMicroServiceUrlBase + Suffix,
    config.vrmAssignFulfilMicroServiceUrlBase + Suffix
)