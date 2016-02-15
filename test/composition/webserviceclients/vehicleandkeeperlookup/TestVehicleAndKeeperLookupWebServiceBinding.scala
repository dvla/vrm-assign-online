package composition.webserviceclients.vehicleandkeeperlookup

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebServiceBinding.createResponse
import org.joda.time.{DateTime, Instant}
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, when}
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.{Json, JsValue}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.DmsWebHeaderDto
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup
import vehicleandkeeperlookup.VehicleAndKeeperLookupFailureResponse
import vehicleandkeeperlookup.VehicleAndKeeperLookupRequest
import vehicleandkeeperlookup.VehicleAndKeeperLookupSuccessResponse
import vehicleandkeeperlookup.VehicleAndKeeperLookupWebService
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseSuccess
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseUnhandledException
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseVRMNotFound
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseDocRefNumberNotLatest

import webserviceclients.fakes.FakeResponse

final class TestVehicleAndKeeperLookupWebServiceBinding(
  // This can be passed in so the calls to the mock can be verified
  webService: VehicleAndKeeperLookupWebService = mock(classOf[VehicleAndKeeperLookupWebService]),
  statusAndResponse: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse, VehicleAndKeeperLookupSuccessResponse]])
  = vehicleAndKeeperDetailsResponseSuccess
  ) extends ScalaModule with MockitoSugar {

  val trackingId = TrackingId("default_test_tracking_id")
  val exceptionTestRequest1 = new VehicleAndKeeperLookupRequest(buildHeader(trackingId),
      "gfw",
      registrationNumber = "H1", // any valid reg number
      Instant.now.toDateTime
  )

    val exceptionTestRequest2 = new VehicleAndKeeperLookupRequest(buildHeader(trackingId),
      "gfw",
      registrationNumber = "I1",
      Instant.now.toDateTime
  )

  val exceptionVnf = new VehicleAndKeeperLookupRequest(buildHeader(trackingId),
      "gfw",
      registrationNumber = "VNF1",
      Instant.now.toDateTime
  )

  val stub = {
    when(webService.invoke(any[VehicleAndKeeperLookupRequest], any[TrackingId]))
      .thenReturn(Future.successful(createResponse(statusAndResponse)))

    when(webService.invoke(Matchers.refEq(exceptionTestRequest1, "dmsHeader", "referenceNumber", "transactionTimestamp"),
                          any[TrackingId]))
      .thenReturn(Future.successful(createResponse(vehicleAndKeeperDetailsResponseUnhandledException))
      )

    when(webService.invoke(Matchers.refEq(exceptionTestRequest2, "dmsHeader", "referenceNumber", "transactionTimestamp"),
                          any[TrackingId]))
      .thenReturn(Future.successful(createResponse(vehicleAndKeeperDetailsResponseUnhandledException))
      )

    when(webService.invoke(Matchers.refEq(exceptionVnf, "dmsHeader", "referenceNumber", "transactionTimestamp"),
                        any[TrackingId]))
     .thenReturn(Future.successful(createResponse(vehicleAndKeeperDetailsResponseVRMNotFound))  //code = VMPR1
    )

    webService
  }

  def configure() = bind[VehicleAndKeeperLookupWebService].toInstance(stub)

  private def buildHeader(trackingId: TrackingId): DmsWebHeaderDto = {
    val alwaysLog = true
    val englishLanguage = "EN"
    DmsWebHeaderDto(conversationId = trackingId.value,
      originDateTime = Instant.now.toDateTime,
      applicationCode = "test-applicationCode",
      channelCode = "test-channelCode",
      contactId = 42,
      eventFlag = alwaysLog,
      serviceTypeCode = "test-dmsServiceTypeCode",
      languageCode = englishLanguage,
      endUser = None)
  }

}

object TestVehicleAndKeeperLookupWebServiceBinding {

  def createResponse(
    response: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse, VehicleAndKeeperLookupSuccessResponse]])) = {
    val (status: Int, dto: Option[JsValue]) = response match {
      case (s, None) => (s, None)
      case (s, Some(Left(failure))) => (s, Some(Json.toJson(failure)))
      case (s, Some(Right(success))) => (s, Some(Json.toJson(success)))
    }
    new FakeResponse(status = status, fakeJson = dto)
  }
}