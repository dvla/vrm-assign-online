package composition

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import utils.helpers.Config

import scala.concurrent.duration.DurationInt

final class TestConfig(
                  isPrototypeBannerVisible: Boolean = true,
                  ordnanceSurveyUseUprn: Boolean = false,
                  auditServiceUseRabbit: Boolean = false,
                  rabbitmqHost: String = "NOT FOUND",
                  rabbitmqPort: Int = 0,
                  rabbitmqQueue: String = "NOT FOUND",
                  vehicleAndKeeperLookupMicroServiceBaseUrl: String = "NOT FOUND",
                  secureCookies: Boolean = true,
                  cookieMaxAge: Int = 30.minutes.toSeconds.toInt,
                  storeBusinessDetailsMaxAge: Int = 7.days.toSeconds.toInt
                  ) extends ScalaModule with MockitoSugar {

  val notFound = "NOT FOUND"

  def build = {
    val config: Config = mock[Config]
    when(config.isCsrfPreventionEnabled).thenReturn(true)
    when(config.vehicleAndKeeperLookupMicroServiceBaseUrl).thenReturn(notFound)
    when(config.vrmAssignEligibilityMicroServiceUrlBase).thenReturn(notFound)
    when(config.vrmAssignFulfilMicroServiceUrlBase).thenReturn(notFound)
    when(config.paymentSolveMicroServiceUrlBase).thenReturn(notFound)
    when(config.paymentSolveMsRequestTimeout).thenReturn(5.seconds.toMillis.toInt)

    when(config.googleAnalyticsTrackingId).thenReturn(None)


    when(config.ordnanceSurveyMicroServiceUrl).thenReturn(notFound)
    when(config.ordnanceSurveyRequestTimeout).thenReturn(5.seconds.toMillis.toInt)
    when(config.ordnanceSurveyUseUprn).thenReturn(ordnanceSurveyUseUprn)

    when(config.vehicleAndKeeperLookupRequestTimeout).thenReturn(30.seconds.toMillis.toInt)
    when(config.vrmAssignEligibilityRequestTimeout).thenReturn(30.seconds.toMillis.toInt)
    when(config.vrmAssignFulfilRequestTimeout).thenReturn(30.seconds.toMillis.toInt)

    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.

    when(config.prototypeSurveyUrl).thenReturn(notFound)
    when(config.prototypeSurveyPrepositionInterval).thenReturn(7.days.toMillis)

    when(config.isProgressBarEnabled).thenReturn(true)

    when(config.auditServiceUseRabbit).thenReturn(auditServiceUseRabbit)
    when(config.rabbitmqHost).thenReturn(rabbitmqHost)
    when(config.rabbitmqPort).thenReturn(rabbitmqPort)
    when(config.rabbitmqQueue).thenReturn(rabbitmqQueue)

    when(config.renewalFee).thenReturn(notFound)

    when(config.emailSmtpHost).thenReturn(notFound)
    when(config.emailSmtpHost).thenReturn(notFound)
    when(config.emailSmtpSsl).thenReturn(false)
    when(config.emailSmtpTls).thenReturn(true)
    when(config.emailSmtpUser).thenReturn(notFound)
    when(config.emailSmtpPassword).thenReturn(notFound)
    when(config.emailWhitelist).thenReturn(None)
    when(config.emailSenderAddress).thenReturn(notFound)

    when(config.vehicleAndKeeperLookupMicroServiceBaseUrl).thenReturn(vehicleAndKeeperLookupMicroServiceBaseUrl)
    when(config.secureCookies).thenReturn(secureCookies)
    when(config.cookieMaxAge).thenReturn(cookieMaxAge)
    when(config.storeBusinessDetailsMaxAge).thenReturn(storeBusinessDetailsMaxAge)

    // Web headers
    when(config.applicationCode).thenReturn("test-applicationCode")
    when(config.serviceTypeCode).thenReturn("test-serviceTypeCode")
    when(config.orgBusinessUnit).thenReturn("test-orgBusinessUnit")
    when(config.channelCode).thenReturn("test-channelCode")
    when(config.contactId).thenReturn(42)

    config
  }

  def configure() = {
    bind[Config].toInstance(build)
  }
}
