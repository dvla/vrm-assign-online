package composition

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import scala.concurrent.duration.DurationInt
import uk.gov.dvla.vehicles.presentation.common.services.SEND.EmailConfiguration
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.From
import utils.helpers.Config

final class TestConfig(isPrototypeBannerVisible: Boolean = TestConfig.DEFAULT_PB_VISIBLE,
                       auditServiceUseRabbit: Boolean = TestConfig.DEFAULT_AUDIT_SERVICE_USE_RABBIT,
                       rabbitmqHost: String = TestConfig.DEFAULT_RABBITMQ_HOST,
                       rabbitmqPort: Int = TestConfig.DEFAULT_RABBITMQ_PORT,
                       rabbitmqQueue: String =  TestConfig.DEFAULT_RABBITMQ_Q,
                       vehicleAndKeeperLookupMicroServiceBaseUrl: String = TestConfig.DEFAULT_BASE_URL,
                       secureCookies: Boolean = TestConfig.DEFAULT_SECURE_COOKIES,
                       cookieMaxAge: Int = TestConfig.DEFAULT_COOKIE_MAX_AGE.minutes.toSeconds.toInt,
                       storeBusinessDetailsMaxAge: Int = TestConfig.DEFAULT_STORE_BUSINESS_DETAILS_MAX_AGE.days.toSeconds.toInt,
                       auditMicroServiceUrlBase: String = TestConfig.DEFAULT_AUDIT_BASE_URL,
                       emailServiceMicroServiceUrlBase: String = TestConfig.DEFAULT_BASE_URL,
                       liveAgentEnvVal: Option[String] = None,
                       liveAgentButtonVal: String = TestConfig.DEFAULT_WEBCHAT_BUTTON,
                       liveAgentOrgVal: String = TestConfig.DEFAULT_WEBCHAT_ORG,
                       liveAgentUrlVal: String = TestConfig.DEFAULT_WEBCHAT_URL,
                       failureCodeBlacklist: Option[List[String]] = TestConfig.DEFAULT_WEBCHAT_BLACKLIST) extends ScalaModule with MockitoSugar {

  val stub = {
    val config: Config = mock[Config]
    when(config.assetsUrl).thenReturn(None)
    when(config.isCsrfPreventionEnabled).thenReturn(TestConfig.CSRF_PREVENTION_ENABLED)
    when(config.vehicleAndKeeperLookupMicroServiceBaseUrl).thenReturn(TestConfig.NotFound)
    when(config.vrmAssignEligibilityMicroServiceUrlBase).thenReturn(TestConfig.NotFound)
    when(config.vrmAssignFulfilMicroServiceUrlBase).thenReturn(TestConfig.NotFound)
    when(config.paymentSolveMicroServiceUrlBase).thenReturn(TestConfig.NotFound)
    when(config.paymentSolveMsRequestTimeout).thenReturn(TestConfig.SOLVE_REQ_TIMEOUT.seconds.toMillis.toInt)

    when(config.googleAnalyticsTrackingId).thenReturn(None)

    when(config.vehicleAndKeeperLookupRequestTimeout).thenReturn(TestConfig.VKL_REQ_TIMEOUT.seconds.toMillis.toInt)
    when(config.vrmAssignEligibilityRequestTimeout).thenReturn(TestConfig.VKL_REQ_TIMEOUT.seconds.toMillis.toInt)
    when(config.vrmAssignFulfilRequestTimeout).thenReturn(TestConfig.VKL_REQ_TIMEOUT.seconds.toMillis.toInt)

    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible)

    when(config.surveyUrl).thenReturn(None)

    when(config.renewalFeeInPence).thenReturn(TestConfig.RENEWAL_FEE)
    when(config.renewalFeeExpiryInYears).thenReturn(TestConfig.RENEWAL_FEE_EXPIRY_IN_YEARS)

    when(config.emailWhitelist).thenReturn(None)
    when(config.emailSenderAddress).thenReturn(TestConfig.NotFound)

    when(config.vehicleAndKeeperLookupMicroServiceBaseUrl).thenReturn(vehicleAndKeeperLookupMicroServiceBaseUrl)
    when(config.encryptCookies).thenReturn(TestConfig.ENCRYPTED_COOKIES)
    when(config.secureCookies).thenReturn(secureCookies)
    when(config.cookieMaxAge).thenReturn(cookieMaxAge)
    when(config.storeBusinessDetailsMaxAge).thenReturn(storeBusinessDetailsMaxAge)

    when(config.auditMicroServiceUrlBase).thenReturn(auditMicroServiceUrlBase)
    when(config.auditMsRequestTimeout).thenReturn(TestConfig.MICROSERVICE_REQ_TIMEOUT)

    when(config.emailServiceMicroServiceUrlBase).thenReturn(emailServiceMicroServiceUrlBase)
    when(config.emailServiceMsRequestTimeout).thenReturn(TestConfig.MICROSERVICE_REQ_TIMEOUT)

    // Web headers
    when(config.applicationCode).thenReturn(TestConfig.WEB_APPLICATION_CODE)
    when(config.vssServiceTypeCode).thenReturn(TestConfig.WEB_VSSSERVICETYPE_CODE)
    when(config.dmsServiceTypeCode).thenReturn(TestConfig.WEB_DMSSERVICETYPE_CODE)
    when(config.channelCode).thenReturn(TestConfig.WEB_CHANNEL_CODE)
    when(config.contactId).thenReturn(TestConfig.WEB_CONTACT_ID)
    when(config.orgBusinessUnit).thenReturn(TestConfig.WEB_ORG_BU)

    // Closing
    when(config.openingTimeMinOfDay).thenReturn(TestConfig.OPENING_TIME)
    when(config.closingTimeMinOfDay).thenReturn(TestConfig.CLOSING_TIME)
    when(config.emailConfiguration).thenReturn(EmailConfiguration(
      from = From(TestConfig.EMAIL_FROM_EMAIL, TestConfig.EMAIL_FROM_NAME),
      feedbackEmail = From(TestConfig.EMAILFEEDBACK_FROM_EMAIL, TestConfig.EMAILFEEDBACK_FROM_NAME),
      whiteList = None
    ))
    when(config.closedDays).thenReturn(TestConfig.CLOSED_DAYS)

    // Web chat enablement
    when(config.liveAgentEnvironmentId).thenReturn(liveAgentEnvVal)

    // Web chat extra config
    when(config.liveAgentButtonId).thenReturn(liveAgentButtonVal)
    when(config.liveAgentOrgId).thenReturn(liveAgentOrgVal)
    when(config.liveAgentUrl).thenReturn(liveAgentUrlVal)

    when(config.failureCodeBlacklist).thenReturn(failureCodeBlacklist)

    config
  }

  def configure() = bind[Config].toInstance(stub)
}

object TestConfig {
  // test data
  final val EMAIL_FROM_NAME = "Someone"
  final val EMAILFEEDBACK_FROM_NAME = "Nobody"
  final val EMAIL_FROM_EMAIL = ""
  final val EMAILFEEDBACK_FROM_EMAIL = ""

  final val NotFound = "NOT FOUND"
  final val SOLVE_REQ_TIMEOUT = 5 // in seconds
  final val VKL_REQ_TIMEOUT = 30 // in seconds
  final val WEB_APPLICATION_CODE = "test-applicationCode"
  final val WEB_VSSSERVICETYPE_CODE = "test-vssServiceTypeCode"
  final val WEB_DMSSERVICETYPE_CODE = "test-dmsServiceTypeCode"
  final val WEB_CHANNEL_CODE = "test-channelCode"
  final val WEB_CONTACT_ID = 42
  final val WEB_ORG_BU = "test-orgBusinessUnit"
  final val MICROSERVICE_REQ_TIMEOUT = 30000 // in millis
  final val OPENING_TIME = 0
  final val CLOSING_TIME = 1439 // in minutes
  final val CSRF_PREVENTION_ENABLED = true
  final val ENCRYPTED_COOKIES = false

  final val RENEWAL_FEE = "17000" // in pence
  final val RENEWAL_FEE_EXPIRY_IN_YEARS = 6
  final val CLOSED_DAYS = List(7) // Sunday

  // defaults
  final val DEFAULT_BASE_URL = NotFound
  final val DEFAULT_AUDIT_BASE_URL = ""
  final val DEFAULT_PB_VISIBLE = true
  final val DEFAULT_RABBITMQ_Q = NotFound
  final val DEFAULT_RABBITMQ_HOST = NotFound
  final val DEFAULT_RABBITMQ_PORT = 0
  final val DEFAULT_SECURE_COOKIES = false
  final val DEFAULT_COOKIE_MAX_AGE = 30
  final val DEFAULT_STORE_BUSINESS_DETAILS_MAX_AGE = 7 // days
  final val DEFAULT_AUDIT_SERVICE_USE_RABBIT = false

  final val DEFAULT_WEBCHAT_BUTTON = "XXX"
  final val DEFAULT_WEBCHAT_ORG = "YYY"
  final val DEFAULT_WEBCHAT_URL = "ZZZ"
  final val DEFAULT_WEBCHAT_BLACKLIST = Some(List("alpha", "bravo", "charlie"))
}
