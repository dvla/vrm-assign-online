package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupConfig
import uk.gov.dvla.vehicles.presentation.common.services.SEND.EmailConfiguration

trait Config extends VehicleLookupConfig {

  val assetsUrl: Option[String]

  val isCsrfPreventionEnabled: Boolean

  // Micro-service config // TODO take defaults off the timeouts
  val vehicleAndKeeperLookupMicroServiceBaseUrl: String
  val vrmAssignEligibilityMicroServiceUrlBase: String
  val vrmAssignFulfilMicroServiceUrlBase: String
  val paymentSolveMicroServiceUrlBase: String
  val paymentSolveMsRequestTimeout: Int

  val emailServiceMicroServiceUrlBase: String
  val emailServiceMsRequestTimeout: Int
  val emailConfiguration: EmailConfiguration

  // Ordnance survey config
  val ordnanceSurveyMicroServiceUrl: String
  val ordnanceSurveyRequestTimeout: Int
  val ordnanceSurveyUseUprn: Boolean

  val vehicleAndKeeperLookupRequestTimeout: Int
  val vrmAssignEligibilityRequestTimeout: Int
  val vrmAssignFulfilRequestTimeout: Int

  // Prototype message in html
  val isPrototypeBannerVisible: Boolean

  // Survey URL
  val surveyUrl: Option[String]

  // Google analytics
  val googleAnalyticsTrackingId: Option[String]

  // Progress step indicator
  val isProgressBarEnabled: Boolean

  // Payment Service
  val renewalFeeInPence: String
  val renewalFeeAbolitionDate: String

  // Email Service
  val emailWhitelist: Option[List[String]]
  //getOptionalProperty[("email.whitelist", "").split(",")
  val emailSenderAddress: String

  // Cookie flags
  val encryptCookies: Boolean
  val secureCookies: Boolean
  val cookieMaxAge: Int
  val storeBusinessDetailsMaxAge: Int

  // Audit microservice
  val auditMicroServiceUrlBase: String
  val auditMsRequestTimeout: Int

  val opening: Int
  val closing: Int
  val openingTimeMinOfDay: Int
  val closingTimeMinOfDay: Int
  val closingWarnPeriodMins: Int
}