package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupConfig
import uk.gov.dvla.vehicles.presentation.common.services.SEND.EmailConfiguration
import uk.gov.dvla.vehicles.presentation.common.utils.helpers.CommonConfig

trait Config extends VehicleLookupConfig with CommonConfig {

  val assetsUrl: Option[String]

  val isCsrfPreventionEnabled: Boolean

  // Micro-service config
  val vehicleAndKeeperLookupMicroServiceBaseUrl: String
  val vrmAssignEligibilityMicroServiceUrlBase: String
  val vrmAssignFulfilMicroServiceUrlBase: String
  val paymentSolveMicroServiceUrlBase: String
  val paymentSolveMsRequestTimeout: Int

  val emailServiceMicroServiceUrlBase: String
  val emailServiceMsRequestTimeout: Int
  val emailConfiguration: EmailConfiguration

  val vehicleAndKeeperLookupRequestTimeout: Int
  val vrmAssignEligibilityRequestTimeout: Int
  val vrmAssignFulfilRequestTimeout: Int

  // Survey URL
  val surveyUrl: Option[String]

  // Google analytics
  val googleAnalyticsTrackingId: Option[String]

  // Payment Service
  val renewalFeeInPence: String
  val renewalFeeExpiryInYears: Int

  // Email Service
  val emailWhitelist: Option[List[String]]
  val emailSenderAddress: String

  // Cookie flags
  val encryptCookies: Boolean
  val secureCookies: Boolean
  val cookieMaxAge: Int
  val storeBusinessDetailsMaxAge: Int

  // Audit microservice
  val auditMicroServiceUrlBase: String
  val auditMsRequestTimeout: Int

  val openingTimeMinOfDay: Int
  val closingTimeMinOfDay: Int
  val closingWarnPeriodMins: Int

  val closedDays: List[Int]

  // Web chat Salesforce live agent
  val liveAgentEnvironmentId: Option[String]
  val liveAgentButtonId: String
  val liveAgentOrgId: String
  val liveAgentUrl: String
  val liveAgentjsUrl: String

  val failureCodeBlacklist: Option[List[String]]
}
