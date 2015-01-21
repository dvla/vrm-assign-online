package utils.helpers

import play.api.Play
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._
import scala.concurrent.duration.DurationInt

class Config {

  lazy val isCsrfPreventionEnabled = getOptionalProperty[Boolean]("csrf.prevention").getOrElse(true)

  // Micro-service config // TODO take defaults off the timeouts
  lazy val vehicleAndKeeperLookupMicroServiceBaseUrl: String = getOptionalProperty[String]("vehicleAndKeeperLookupMicroServiceUrlBase").getOrElse("NOT FOUND")
  lazy val vrmAssignEligibilityMicroServiceUrlBase: String = getOptionalProperty[String]("vrmAssignEligibilityMicroServiceUrlBase").getOrElse("NOT FOUND")
  lazy val vrmAssignFulfilMicroServiceUrlBase: String = getOptionalProperty[String]("vrmAssignFulfilMicroServiceUrlBase").getOrElse("NOT FOUND")
  lazy val paymentSolveMicroServiceUrlBase: String = getOptionalProperty[String]("paymentSolveMicroServiceUrlBase").getOrElse("NOT FOUND")
  lazy val paymentSolveMsRequestTimeout: Int = getOptionalProperty[Int]("paymentSolve.ms.requesttimeout").getOrElse( 5.seconds.toMillis.toInt)

  // Ordnance survey config
  lazy val ordnanceSurveyMicroServiceUrl: String = getOptionalProperty[String]("ordnancesurvey.ms.url").getOrElse("NOT FOUND")
  lazy val ordnanceSurveyRequestTimeout: Int = getOptionalProperty[Int]("ordnancesurvey.requesttimeout").getOrElse( 5.seconds.toMillis.toInt)
  lazy val ordnanceSurveyUseUprn: Boolean = getOptionalProperty[Boolean]("ordnancesurvey.useUprn").getOrElse(false)//, default = false)

  lazy val vehicleAndKeeperLookupRequestTimeout: Int = getOptionalProperty[Int]("vehicleAndKeeperLookup.requesttimeout").getOrElse( 30.seconds.toMillis.toInt)
  lazy val vrmAssignEligibilityRequestTimeout: Int = getOptionalProperty[Int]("vrmAssignEligibility.requesttimeout").getOrElse( 30.seconds.toMillis.toInt)
  lazy val vrmAssignFulfilRequestTimeout: Int = getOptionalProperty[Int]("vrmAssignFulfil.requesttimeout").getOrElse( 30.seconds.toMillis.toInt)

  // Prototype message in html
  lazy val isPrototypeBannerVisible: Boolean = getOptionalProperty[Boolean]("prototype.disclaimer").getOrElse(true)//, default = true)

  // Prototype survey URL
  lazy val prototypeSurveyUrl: String = getOptionalProperty[String]("survey.url").getOrElse("")//, "")
  lazy val prototypeSurveyPrepositionInterval: Long = getOptionalProperty[Long]("survey.interval").getOrElse( 7.days.toMillis)

  // Google analytics
  lazy val googleAnalyticsTrackingId: Option[String] = getOptionalProperty[String]("googleAnalytics.id.assign")

  // Progress step indicator
  lazy val isProgressBarEnabled: Boolean = getOptionalProperty[Boolean]("progressBar.enabled").getOrElse(true)//, default = true)

  // Rabbit-MQ
  lazy val rabbitmqHost = getOptionalProperty[String]("rabbitmq.host").getOrElse("NOT FOUND")
  lazy val rabbitmqPort = getOptionalProperty[Int]("rabbitmq.port").getOrElse(0)
  lazy val rabbitmqQueue = getOptionalProperty[String]("rabbitmq.queue").getOrElse("NOT FOUND")
  lazy val rabbitmqUsername = getOptionalProperty[String]("rabbitmq.username").getOrElse("NOT FOUND")
  lazy val rabbitmqPassword = getOptionalProperty[String]("rabbitmq.password").getOrElse("NOT FOUND")
  lazy val rabbitmqVirtualHost = getOptionalProperty[String]("rabbitmq.virtualHost").getOrElse("NOT FOUND")

  // Payment Service
  lazy val renewalFee: String = getOptionalProperty[String]("assign.renewalFee.price").getOrElse("NOT FOUND")//, "NOT FOUND")
  lazy val renewalFeeAbolitionDate: String = getOptionalProperty[String]("assign.renewalFee.abolitionDate").getOrElse("NOT FOUND")//, "NOT FOUND")

  // Email Service
  lazy val emailSmtpHost: String = getOptionalProperty[String]("smtp.host").getOrElse("")
  lazy val emailSmtpPort: Int = getOptionalProperty[Int]("smtp.port").getOrElse(25)//, 25)
  lazy val emailSmtpSsl: Boolean = getOptionalProperty[Boolean]("smtp.ssl").getOrElse(false)//, default = false)
  lazy val emailSmtpTls: Boolean = getOptionalProperty[Boolean]("smtp.tls").getOrElse(true)//, default = true)
  lazy val emailSmtpUser: String = getOptionalProperty[String]("smtp.user").getOrElse("")
  lazy val emailSmtpPassword: String = getOptionalProperty[String]("smtp.password").getOrElse("")
  lazy val emailWhitelist: Option[List[String]] = getOptionalProperty[String]("email.whitelist").map(_.split(",").toList)
  //getOptionalProperty[("email.whitelist", "").split(",")
  lazy val emailSenderAddress: String = getOptionalProperty[String]("email.senderAddress").getOrElse("")//, "")

  // Cookie flags
  lazy val secureCookies = getOptionalProperty[Boolean]("secureCookies").getOrElse(true)//, default = true)
  lazy val cookieMaxAge = getOptionalProperty[Int]("application.cookieMaxAge").getOrElse(30.minutes.toSeconds.toInt)
  lazy val storeBusinessDetailsMaxAge = getOptionalProperty[Int]("storeBusinessDetails.cookieMaxAge").getOrElse(7.days.toSeconds.toInt)

  // Web headers
  val applicationCode: String = getProperty[String]("webHeader.applicationCode")
  val serviceTypeCode: String = getProperty[String]("webHeader.serviceTypeCode")
  val orgBusinessUnit: String = getProperty[String]("webHeader.orgBusinessUnit")
  val channelCode: String = getProperty[String]("webHeader.channelCode")
  val contactId: Long = getProperty[Long]("webHeader.contactId")
}