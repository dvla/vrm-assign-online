package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.booleanProp
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getOptionalProperty
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getStringListProperty
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.intProp
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.longProp
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.stringProp
import uk.gov.dvla.vehicles.presentation.common.services.SEND.EmailConfiguration
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.From

class ConfigImpl extends Config {

  override val assetsUrl: Option[String] = getOptionalProperty[String]("assets.url")

  override val isCsrfPreventionEnabled = getProperty[Boolean]("csrf.prevention")

  // Micro-service config
  override val vehicleAndKeeperLookupMicroServiceBaseUrl: String =
    getProperty[String]("vehicleAndKeeperLookupMicroServiceUrlBase")
  override val vrmAssignEligibilityMicroServiceUrlBase: String =
    getProperty[String]("vrmAssignEligibilityMicroServiceUrlBase")
  override val vrmAssignFulfilMicroServiceUrlBase: String = getProperty[String]("vrmAssignFulfilMicroServiceUrlBase")
  override val paymentSolveMicroServiceUrlBase: String = getProperty[String]("paymentSolveMicroServiceUrlBase")
  override val paymentSolveMsRequestTimeout: Int = getProperty[Int]("paymentSolve.ms.requesttimeout")

  // Ordnance survey config
  override val ordnanceSurveyMicroServiceUrl: String = getProperty[String]("ordnancesurvey.ms.url")
  override val ordnanceSurveyRequestTimeout: Int = getProperty[Int]("ordnancesurvey.requestTimeout")

  override val vehicleAndKeeperLookupRequestTimeout: Int = getProperty[Int]("vehicleAndKeeperLookup.requesttimeout")
  override val vrmAssignEligibilityRequestTimeout: Int = getProperty[Int]("vrmAssignEligibility.requestTimeout")
  override val vrmAssignFulfilRequestTimeout: Int = getProperty[Int]("vrmAssignFulfil.requestTimeout")

  // Prototype message in html
  override val isPrototypeBannerVisible: Boolean = getProperty[Boolean]("prototype.disclaimer")

  // Survey URL
  override val surveyUrl: Option[String] = getOptionalProperty[String]("survey.url")

  // Google analytics
  override val googleAnalyticsTrackingId: Option[String] = getOptionalProperty[String]("googleAnalytics.id.assign")

  // Payment Service
  override val renewalFeeInPence: String = getProperty[String]("assign.renewalFee.price")
  override val renewalFeeAbolitionDate: String = getProperty[String]("assign.renewalFee.abolitionDate")

  // Email Service
  override val emailWhitelist: Option[List[String]] = getStringListProperty("email.whitelist")
  //getProperty[("email.whitelist", "").split(",")
  override val emailSenderAddress: String = getProperty[String]("email.senderAddress")
  override val emailConfiguration: EmailConfiguration = EmailConfiguration(
    From(getProperty[String]("email.senderAddress"), "DO NOT REPLY"),
    From(getProperty[String]("email.feedbackAddress"), "Feedback"),
    getStringListProperty("email.whitelist")
  )

  // Cookie flags
  override val encryptCookies = getProperty[Boolean]("encryptCookies")
  override val secureCookies = getProperty[Boolean]("secureCookies")
  override val cookieMaxAge = getProperty[Int]("application.cookieMaxAge")
  override val storeBusinessDetailsMaxAge = getProperty[Int]("storeBusinessDetails.cookieMaxAge")

  // Audit microservice
  override val auditMicroServiceUrlBase: String = getProperty[String]("auditMicroServiceUrlBase")
  override val auditMsRequestTimeout: Int = getProperty[Int]("audit.requesttimeout")

  // Email microservice
  override val emailServiceMicroServiceUrlBase: String = getProperty[String]("emailServiceMicroServiceUrlBase")
  override val emailServiceMsRequestTimeout: Int = getProperty[Int]("emailService.ms.requesttimeout")

  // Web headers
  override val applicationCode: String = getProperty[String]("webHeader.applicationCode")
  override val vssServiceTypeCode: String = getProperty[String]("webHeader.vssServiceTypeCode")
  override val dmsServiceTypeCode: String = getProperty[String]("webHeader.dmsServiceTypeCode")
  override val orgBusinessUnit: String = getProperty[String]("webHeader.orgBusinessUnit")
  override val channelCode: String = getProperty[String]("webHeader.channelCode")
  override val contactId: Long = getProperty[Long]("webHeader.contactId")

  override val openingTimeMinOfDay: Int = getProperty[Int]("openingTimeMinOfDay")
  override val closingTimeMinOfDay: Int = getProperty[Int]("closingTimeMinOfDay")
  override val closingWarnPeriodMins: Int = getOptionalProperty[Int]("closingWarnPeriodMins").getOrElse(15)
}