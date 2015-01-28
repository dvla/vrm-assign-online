package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._

class ConfigImpl extends Config {

  val isCsrfPreventionEnabled = getProperty[Boolean]("csrf.prevention")

  // Micro-service config
  val vehicleAndKeeperLookupMicroServiceBaseUrl: String = getProperty[String]("vehicleAndKeeperLookupMicroServiceUrlBase")
  val vrmAssignEligibilityMicroServiceUrlBase: String = getProperty[String]("vrmAssignEligibilityMicroServiceUrlBase")
  val vrmAssignFulfilMicroServiceUrlBase: String = getProperty[String]("vrmAssignFulfilMicroServiceUrlBase")
  val paymentSolveMicroServiceUrlBase: String = getProperty[String]("paymentSolveMicroServiceUrlBase")
  val paymentSolveMsRequestTimeout: Int = getProperty[Int]("paymentSolve.ms.requesttimeout")

  // Ordnance survey config
  val ordnanceSurveyMicroServiceUrl: String = getProperty[String]("ordnancesurvey.ms.url")
  val ordnanceSurveyRequestTimeout: Int = getProperty[Int]("ordnancesurvey.requestTimeout")
  val ordnanceSurveyUseUprn: Boolean = getProperty[Boolean]("ordnancesurvey.useUprn")

  val vehicleAndKeeperLookupRequestTimeout: Int = getProperty[Int]("vehicleAndKeeperLookup.requesttimeout")
  val vrmAssignEligibilityRequestTimeout: Int = getProperty[Int]("vrmAssignEligibility.requestTimeout")
  val vrmAssignFulfilRequestTimeout: Int = getProperty[Int]("vrmAssignFulfil.requestTimeout")

  // Prototype message in html
  val isPrototypeBannerVisible: Boolean = getProperty[Boolean]("prototype.disclaimer")

  // Prototype survey URL
  val prototypeSurveyUrl: String = getOptionalProperty[String]("survey.url").getOrElse("")
  val prototypeSurveyPrepositionInterval: Long = getDurationProperty("survey.interval")

  // Google analytics
  val googleAnalyticsTrackingId: Option[String] = getOptionalProperty[String]("googleAnalytics.id.assign")

  // Progress step indicator
  val isProgressBarEnabled: Boolean = getProperty[Boolean]("progressBar.enabled")

  // Rabbit-MQ
  val rabbitmqHost = getProperty[String]("rabbitmq.host")
  val rabbitmqPort = getProperty[Int]("rabbitmq.port")
  val rabbitmqQueue = getProperty[String]("rabbitmq.queue")
  val rabbitmqUsername = getProperty[String]("rabbitmq.username")
  val rabbitmqPassword = getProperty[String]("rabbitmq.password")
  val rabbitmqVirtualHost = getProperty[String]("rabbitmq.virtualHost")

  // Payment Service
  val renewalFee: String = getProperty[String]("assign.renewalFee.price")
  val renewalFeeAbolitionDate: String = getProperty[String]("assign.renewalFee.abolitionDate")

  // Email Service
  val emailSmtpHost: String = getProperty[String]("smtp.host")
  val emailSmtpPort: Int = getProperty[Int]("smtp.port")
  val emailSmtpSsl: Boolean = getProperty[Boolean]("smtp.ssl")
  val emailSmtpTls: Boolean = getProperty[Boolean]("smtp.tls")
  val emailSmtpUser: String = getProperty[String]("smtp.user")
  val emailSmtpPassword: String = getProperty[String]("smtp.password")
  val emailWhitelist: Option[List[String]] = getOptionalProperty[String]("email.whitelist").map(_.split(",").toList)
  //getProperty[("email.whitelist", "").split(",")
  val emailSenderAddress: String = getProperty[String]("email.senderAddress")

  // Cookie flags
  val encryptCookies = getProperty[Boolean]("encryptCookies")
  val secureCookies = getProperty[Boolean]("secureCookies")
  val cookieMaxAge = getProperty[Int]("application.cookieMaxAge")
  val storeBusinessDetailsMaxAge = getProperty[Int]("storeBusinessDetails.cookieMaxAge")

  // Audit microservice
  val auditMicroServiceUrlBase: String = getProperty[String]("auditMicroServiceUrlBase")
  val auditMsRequestTimeout: Int = getProperty[Int]("audit.requesttimeout")

  // Web headers
  val applicationCode: String = getProperty[String]("webHeader.applicationCode")
  val serviceTypeCode: String = getProperty[String]("webHeader.serviceTypeCode")
  val orgBusinessUnit: String = getProperty[String]("webHeader.orgBusinessUnit")
  val channelCode: String = getProperty[String]("webHeader.channelCode")
  val contactId: Long = getProperty[Long]("webHeader.contactId")
}