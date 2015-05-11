package composition

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.dvla.vehicles.presentation.common.services.SEND.EmailConfiguration
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.From
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
                        secureCookies: Boolean = false,
                        cookieMaxAge: Int = 30.minutes.toSeconds.toInt,
                        storeBusinessDetailsMaxAge: Int = 7.days.toSeconds.toInt,
                        auditMicroServiceUrlBase: String = "",
                        emailServiceMicroServiceUrlBase: String = "NOT FOUND"
                        ) extends ScalaModule with MockitoSugar {

  private val notFound = "NOT FOUND"

  val stub = {
    val config: Config = mock[Config]
    when(config.assetsUrl).thenReturn(None)
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

    when(config.renewalFee).thenReturn("8000")
    when(config.renewalFeeAbolitionDate).thenReturn("09/03/2015")

    when(config.emailWhitelist).thenReturn(None)
    when(config.emailSenderAddress).thenReturn(notFound)

    when(config.vehicleAndKeeperLookupMicroServiceBaseUrl).thenReturn(vehicleAndKeeperLookupMicroServiceBaseUrl)
    when(config.encryptCookies).thenReturn(false)
    when(config.secureCookies).thenReturn(secureCookies)
    when(config.cookieMaxAge).thenReturn(cookieMaxAge)
    when(config.storeBusinessDetailsMaxAge).thenReturn(storeBusinessDetailsMaxAge)

    when(config.auditMicroServiceUrlBase).thenReturn(auditMicroServiceUrlBase)
    when(config.auditMsRequestTimeout).thenReturn(30000)

    when(config.emailServiceMicroServiceUrlBase).thenReturn(emailServiceMicroServiceUrlBase)
    when(config.emailServiceMsRequestTimeout).thenReturn(30000)

    // Web headers
    when(config.applicationCode).thenReturn("test-applicationCode")
    when(config.dmsServiceTypeCode).thenReturn("test-dmsServiceTypeCode")
    when(config.vssServiceTypeCode).thenReturn("test-vssServiceTypeCode")
    when(config.orgBusinessUnit).thenReturn("test-orgBusinessUnit")
    when(config.channelCode).thenReturn("test-channelCode")
    when(config.contactId).thenReturn(42)

    // Closing
    when(config.opening).thenReturn(0)
    when(config.closing).thenReturn(23)
    when(config.emailConfiguration).thenReturn(EmailConfiguration(
      from = From("", "DO-NOT-REPLY"),
      feedbackEmail = From("", "Feedback"),
      whiteList = None
    ))

    config
  }

  def configure() = bind[Config].toInstance(stub)
}
