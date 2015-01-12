package utils.helpers

import play.api.Play
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._
import scala.concurrent.duration.DurationInt

class Config {

  lazy val isCsrfPreventionEnabled = getProperty[Boolean]("csrf.prevention")//, default = true)

  // Micro-service config // TODO take defaults off the timeouts
  lazy val vehicleAndKeeperLookupMicroServiceBaseUrl: String = getProperty[String]("vehicleAndKeeperLookupMicroServiceUrlBase")//, "NOT FOUND")
  lazy val vrmAssignEligibilityMicroServiceUrlBase: String = getProperty[String]("vrmAssignEligibilityMicroServiceUrlBase")//, "NOT FOUND")
  lazy val vrmAssignFulfilMicroServiceUrlBase: String = getProperty[String]("vrmAssignFulfilMicroServiceUrlBase")//, "NOT FOUND")
  lazy val paymentSolveMicroServiceUrlBase: String = getProperty[String]("paymentSolveMicroServiceUrlBase")//, "NOT FOUND")
  lazy val paymentSolveMsRequestTimeout: Int = getProperty[Int]("paymentSolve.ms.requesttimeout")//, 5.seconds.toMillis.toInt)

  // Ordnance survey config
  lazy val ordnanceSurveyMicroServiceUrl: String = getProperty[String]("ordnancesurvey.ms.url")//, "NOT FOUND")
  lazy val ordnanceSurveyRequestTimeout: Int = getProperty[Int]("ordnancesurvey.requesttimeout")//, 5.seconds.toMillis.toInt)
  lazy val ordnanceSurveyUseUprn: Boolean = getProperty[Boolean]("ordnancesurvey.useUprn")//, default = false)

  lazy val vehicleAndKeeperLookupRequestTimeout: Int = getProperty[Int]("vehicleAndKeeperLookup.requesttimeout")//, 30.seconds.toMillis.toInt)
  lazy val vrmAssignEligibilityRequestTimeout: Int = getProperty[Int]("vrmAssignEligibility.requesttimeout")//, 30.seconds.toMillis.toInt)
  lazy val vrmAssignFulfilRequestTimeout: Int = getProperty[Int]("vrmAssignFulfil.requesttimeout")//, 30.seconds.toMillis.toInt)

  // Prototype message in html
  lazy val isPrototypeBannerVisible: Boolean = getProperty[Boolean]("prototype.disclaimer")//, default = true)

  // Prototype survey URL
  lazy val prototypeSurveyUrl: String = getProperty[String]("survey.url")//, "")
  lazy val prototypeSurveyPrepositionInterval: Long = getProperty[Long]("survey.interval")//, 7.days.toMillis)

  // Google analytics
  lazy val googleAnalyticsTrackingId: Option[String] = getOptionalProperty[String]("googleAnalytics.id.assign")//, "NOT FOUND")

  // Progress step indicator
  lazy val isProgressBarEnabled: Boolean = getProperty[Boolean]("progressBar.enabled")//, default = true)

  // Audit Service
  lazy val auditServiceUseRabbit = getProperty[Boolean]("auditService.useRabbit")//, default = false)

  // Rabbit-MQ
  lazy val rabbitmqHost = getProperty[String]("rabbitmq.host")//, "NOT FOUND")
  lazy val rabbitmqPort = getProperty[Int]("rabbitmq.port")//, 0)
  lazy val rabbitmqQueue = getProperty[String]("rabbitmq.queue")//, "NOT FOUND")

  // Payment Service
  lazy val renewalFee: String = getProperty[String]("assign.renewalFee")//, "NOT FOUND")

  // Email Service
  lazy val emailSmtpHost: String = getProperty[String]("smtp.host")//, "")
  lazy val emailSmtpPort: Int = getProperty[Int]("smtp.port")//, 25)
  lazy val emailSmtpSsl: Boolean = getProperty[Boolean]("smtp.ssl")//, default = false)
  lazy val emailSmtpTls: Boolean = getProperty[Boolean]("smtp.tls")//, default = true)
  lazy val emailSmtpUser: String = getProperty[String]("smtp.user")//, "")
  lazy val emailSmtpPassword: String = getProperty[String]("smtp.password")//, "")
  lazy val emailWhitelist: Option[List[String]] = getStringListProperty("email.whitelist") //getProperty[("email.whitelist", "").split(",")
  lazy val emailSenderAddress: String = getProperty[String]("email.senderAddress")//, "")

  // Cookie flags
  lazy val secureCookies = getProperty[Boolean]("secureCookies")//, default = true)
  lazy val cookieMaxAge = getProperty[Int]("application.cookieMaxAge")//, 30.minutes.toSeconds.toInt)
  lazy val storeBusinessDetailsMaxAge = getProperty[Int]("storeBusinessDetails.cookieMaxAge")//, 7.days.toSeconds.toInt)
}