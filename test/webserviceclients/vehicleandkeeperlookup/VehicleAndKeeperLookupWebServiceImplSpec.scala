package webserviceclients.vehicleandkeeperlookup

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import helpers.TestWithApplication
import helpers.UnitSpec
import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.TrackingId
import common.testhelpers.WireMockFixture
import common.webserviceclients.HttpHeaders
import common.webserviceclients.common.DmsWebHeaderDto
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupConfig
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupRequest
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupWebServiceImpl
import webserviceclients.fakes.DateServiceConstants.{DayValid, MonthValid, YearValid}

class VehicleAndKeeperLookupWebServiceImplSpec extends UnitSpec with WireMockFixture {

  "callVehicleAndKeeperLookupService" should {
    "send the serialised json request" in new TestWithApplication {
      val resultFuture = lookupService.invoke(request, trackingId)
      whenReady(resultFuture, timeout) { result =>
        wireMock.verifyThat(1, postRequestedFor(
          urlEqualTo(s"/vehicleandkeeper/lookup/v1")
        ).withHeader(HttpHeaders.TrackingId, equalTo(trackingId.value)))
      }
    }
  }

  private def lookupService = new VehicleAndKeeperLookupWebServiceImpl(
    new VehicleAndKeeperLookupConfig() {
      override lazy val vehicleAndKeeperLookupMicroServiceBaseUrl = s"http://localhost:$wireMockPort"
    }
  )

  private final val trackingId = TrackingId("track-id-test")

  private def dateTime = new DateTime(
    YearValid.toInt,
    MonthValid.toInt,
    DayValid.toInt,
    0,
    0)

  private def request = VehicleAndKeeperLookupRequest(
    dmsHeader = buildHeader(trackingId),
    referenceNumber = "ref number",
    registrationNumber = "reg number",
    transactionTimestamp = dateTime
  )

  private def buildHeader(trackingId: TrackingId): DmsWebHeaderDto = {
    val alwaysLog = true
    val englishLanguage = "EN"
    DmsWebHeaderDto(conversationId = trackingId.value,
      originDateTime = dateTime,
      applicationCode = "test-applicationCode",
      channelCode = "test-channelCode",
      contactId = 42,
      eventFlag = alwaysLog,
      serviceTypeCode = "test-serviceTypeCode",
      languageCode = englishLanguage,
      endUser = None)
  }

  private implicit val vehicleAndKeeperDetailsFormat = Json.format[VehicleAndKeeperLookupRequest]
}
