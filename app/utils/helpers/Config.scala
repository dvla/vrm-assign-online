package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupConfig
import uk.gov.dvla.vehicles.presentation.common.services.SEND.EmailConfiguration

trait Config extends VehicleLookupConfig {

  def assetsUrl: Option[String]

  def isCsrfPreventionEnabled: Boolean

  // Micro-service config // TODO take defaults off the timeouts
  def vehicleAndKeeperLookupMicroServiceBaseUrl: String
  def vrmAssignEligibilityMicroServiceUrlBase: String
  def vrmAssignFulfilMicroServiceUrlBase: String
  def paymentSolveMicroServiceUrlBase: String
  def paymentSolveMsRequestTimeout: Int

  def emailServiceMicroServiceUrlBase: String
  def emailServiceMsRequestTimeout: Int
  def emailConfiguration: EmailConfiguration

  // Ordnance survey config
  def ordnanceSurveyMicroServiceUrl: String
  def ordnanceSurveyRequestTimeout: Int
  def ordnanceSurveyUseUprn: Boolean

  def vehicleAndKeeperLookupRequestTimeout: Int
  def vrmAssignEligibilityRequestTimeout: Int
  def vrmAssignFulfilRequestTimeout: Int

  // Prototype message in html
  def isPrototypeBannerVisible: Boolean

  // Prototype survey URL
  def prototypeSurveyUrl: String
  def prototypeSurveyPrepositionInterval: Long

  // Google analytics
  def googleAnalyticsTrackingId: Option[String]

  // Progress step indicator
  def isProgressBarEnabled: Boolean

  // Rabbit-MQ
  def rabbitmqHost: String
  def rabbitmqPort: Int
  def rabbitmqQueue: String
  def rabbitmqUsername:String
  def rabbitmqPassword: String
  def rabbitmqVirtualHost: String

  // Payment Service
  def renewalFee: String
  def renewalFeeAbolitionDate: String

  // Email Service
  def emailWhitelist: Option[List[String]]
  //getOptionalProperty[("email.whitelist", "").split(",")
  def emailSenderAddress: String

  // Cookie flags
  def encryptCookies: Boolean
  def secureCookies: Boolean
  def cookieMaxAge: Int
  def storeBusinessDetailsMaxAge:Int

  // Audit microservice
  def auditMicroServiceUrlBase: String
  def auditMsRequestTimeout: Int

  def opening: Int
  def closing: Int
  def closingWarnPeriodMins: Int
}