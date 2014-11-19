package controllers

import audit.{AuditMessage, AuditService}
import composition._
import composition.vehicleandkeeperlookup._
import controllers.Common.PrototypeHtml
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_assign.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import models.{VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel}
import org.mockito.Mockito._
import pages.vrm_assign._
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, contentAsString, defaultAwaitTimeout}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.mappings.DocumentReferenceNumber
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import views.vrm_assign.VehicleLookup._
import webserviceclients.fakes.AddressLookupServiceConstants.PostcodeValid
import webserviceclients.fakes.BruteForcePreventionWebServiceConstants.VrmLocked
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, when}
import org.mockito.internal.stubbing.answers.DoesNothing
import org.scalatest.mock.MockitoSugar
import uk.gov.dvla.auditing.Message
import webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsResponse
import helpers.JsonUtils.deserializeJsonToModel

final class VehicleLookupUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      present.futureValue.header.status should equal(play.api.http.Status.OK)
    }

    "display populated fields when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel())
      val result = vehicleLookupStubs().present(request)
      val content = contentAsString(result)
      content should include(ReferenceNumberValid)
      content should include(RegistrationNumberValid)
    }

    "display empty fields when cookie does not exist" in new WithApplication {
      val request = FakeRequest()
      val result = vehicleLookupStubs().present(request)
      val content = contentAsString(result)
      content should not include ReferenceNumberValid
      content should not include RegistrationNumberValid
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      val result = vehicleLookupStubs(isPrototypeBannerVisible = false).present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {
    "redirect to Confirm after a valid submit and true message returned from the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(postcode = KeeperPostcodeValidForMicroService)
      val result = vehicleLookupStubs().submit(request)

      whenReady(result, timeout) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(CaptureCertificateDetailsPage.address))
          val cookies = fetchCookiesFromHeaders(r)
          val cookieName = VehicleAndKeeperLookupFormModelCacheKey
          cookies.find(_.name == cookieName) match {
            case Some(cookie) =>
              val json = cookie.value
              val model = deserializeJsonToModel[VehicleAndKeeperLookupFormModel](json)
              model.registrationNumber should equal(RegistrationNumberValid.toUpperCase)
            case None => fail(s"$cookieName cookie not found")
          }

          val cookie2Name = VehicleAndKeeperLookupDetailsCacheKey
          cookies.find(_.name == cookie2Name) match {
            case Some(cookie) =>
              val json = cookie.value
              deserializeJsonToModel[VehicleAndKeeperDetailsModel](json)
            case None => fail(s"$cookie2Name cookie not found")
          }
      }
    }

    "submit removes spaces from registrationNumber" in new WithApplication {
      // DE7 Spaces should be stripped
      val request = buildCorrectlyPopulatedRequest(registrationNumber = RegistrationNumberWithSpaceValid)
      val result = vehicleLookupStubs().submit(request)

      whenReady(result) {
        r =>
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should contain(VehicleAndKeeperLookupFormModelCacheKey)
      }
    }

    "redirect to MicroServiceError after a submit and no response code and no vehicledetailsdto returned from the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseNotFoundResponseCode).submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    }
//
//    "redirect to VehicleAndKeeperLookupFailure after a submit and vrm not found by the fake microservice" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleLookupStubs(vehicleDetailsResponseVRMNotFound).submit(request)
//
//      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleAndKeeperLookupFailurePage.address))
//    }
//
//    "redirect to VehicleAndKeeperLookupFailure after a submit and document reference number mismatch returned by the fake microservice" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleLookupStubs(vehicleDetailsResponseDocRefNumberNotLatest).submit(request)
//
//      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleAndKeeperLookupFailurePage.address))
//    }
//
//    "redirect to VehicleAndKeeperLookupFailure after a submit and vss error returned by the fake microservice" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleLookupStubs(vehicleDetailsServerDown).submit(request)
//
//      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
//    }

    "replace max length error message for document reference number with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "1" * (DocumentReferenceNumber.MaxLength + 1))
      val result = vehicleLookupStubs().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace required and min length error messages for document reference number with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "")
      val result = vehicleLookupStubs().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace max length error message for vehicle registration mark with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "PJ05YYYX")
      val result = vehicleLookupStubs().submit(request)
      val count = "Must be valid format".r.findAllIn(contentAsString(result)).length

      count should equal(2)
    }

    "replace required and min length error messages for vehicle registration mark with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "")
      val result = vehicleLookupStubs().submit(request)
      val count = "Must be valid format".r.findAllIn(contentAsString(result)).length

      count should equal(2) // The same message is displayed in 2 places - once in the validation-summary at the top of the page and once above the field.
    }

    "redirect to MicroserviceError page when vehicleAndKeeperLookup throws an exception" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperLookupCallFails().submit(request)

      whenReady(result, timeout) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "does not write VehicleAndKeeperDetailsModel cookie when microservice throws an exception" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperLookupCallFails().submit(request)

      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should not contain VehicleAndKeeperLookupDetailsCacheKey
      }
    }

    "redirect to MicroServiceError after a submit if response status is Ok and no response payload" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallNoResponse().submit(request)

      whenReady(result, timeout) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "write cookie when vss error returned by the microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallServerDown().submit(request)

      whenReady(result) {
        r =>
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should contain(VehicleAndKeeperLookupFormModelCacheKey)
      }
    }

    // TODO need to revisit this, can't work out why it fails after audit work
//    "write cookie when vrm not found by the fake microservice" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleAndKeeperDetailsCallVRMNotFound().submit(request)
//      whenReady(result) {
//        r =>
//          val cookies = fetchCookiesFromHeaders(r)
//          cookies.map(_.name) should contain allOf(
//            PaymentTransNoCacheKey, TransactionIdCacheKey, BruteForcePreventionViewModelCacheKey,
//            VehicleAndKeeperLookupResponseCodeCacheKey, VehicleAndKeeperLookupFormModelCacheKey)
//      }
//    }

    "redirect to vrm locked when valid submit and brute force prevention returns not permitted" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = VrmLocked)
      val result = vehicleLookupStubs(permitted = false).submit(request)
      result.futureValue.header.headers.get(LOCATION) should equal(Some(VrmLockedPage.address))
    }

    // TODO need to revisit this, can't work out why it fails after audit work
//    "redirect to VehicleAndKeeperLookupFailure and display 1st attempt message when document reference number not found and security service returns 1st attempt" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleAndKeeperDetailsCallDocRefNumberNotLatest().submit(request)
//
//      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
//    }

    // TODO need to revisit this, can't work out why it fails after audit work
//    "write cookie when document reference number mismatch returned by microservice" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleAndKeeperDetailsCallDocRefNumberNotLatest().submit(request)
//      whenReady(result) {
//        r =>
//          val cookies = fetchCookiesFromHeaders(r)
//          cookies.map(_.name) should contain allOf(
//            BruteForcePreventionViewModelCacheKey, VehicleAndKeeperLookupResponseCodeCacheKey, VehicleAndKeeperLookupFormModelCacheKey)
//      }
//    }

//    "redirect to VehicleAndKeeperLookupFailure and display 2nd attempt message when document reference number not found and security service returns 2nd attempt" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest(registrationNumber = VrmAttempt2)
//      val result = vehicleLookupStubs(
//        vehicleDetailsResponseDocRefNumberNotLatest,
//        bruteForceService = bruteForceServiceImpl(permitted = true)
//      ).submit(request)
//
//      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleAndKeeperLookupFailurePage.address))
//    }
//
//    "Send a request and a trackingId" in new WithApplication {
//      val trackingId = "x" * 20
//      val request = buildCorrectlyPopulatedRequest().
//        withCookies(CookieFactoryForUnitSpecs.trackingIdModel(trackingId))
//      val mockVehiclesLookupService = mock[VehicleAndKeeperLookupWebService]
//      when(mockVehiclesLookupService.callVehicleAndKeeperLookupService(any[VehicleDetailsRequest], any[String])).
//        thenReturn(Future {
//        new FakeResponse(status = 200, fakeJson = Some(Json.toJson(vehicleDetailsResponseSuccess._2.get)))
//      })
//      val vehicleAndKeeperLookupServiceImpl = new VehicleAndKeeperLookupServiceImpl(mockVehiclesLookupService)
//      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
//      implicit val config: Config = mock[Config]
//
//      val vehiclesLookup = new vrm_assign.VehicleAndKeeperLookup(
//        bruteForceServiceImpl(permitted = true),
//        vehicleAndKeeperLookupServiceImpl
//      )
//      val result = vehiclesLookup.submit(request)
//
//      whenReady(result) {
//        r =>
//          val trackingIdCaptor = ArgumentCaptor.forClass(classOf[String])
//          verify(mockVehiclesLookupService).callVehicleAndKeeperLookupService(any[VehicleDetailsRequest], trackingIdCaptor.capture())
//          trackingIdCaptor.getValue should be(trackingId)
//      }
//    }
//
//    "Send the request and no trackingId if session is not present" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val mockVehiclesLookupService = mock[VehicleAndKeeperLookupWebService]
//      when(mockVehiclesLookupService.callVehicleAndKeeperLookupService(any[VehicleDetailsRequest], any[String])).thenReturn(Future {
//        new FakeResponse(status = 200, fakeJson = Some(Json.toJson(vehicleDetailsResponseSuccess._2.get)))
//      })
//      val vehicleAndKeeperLookupServiceImpl = new VehicleAndKeeperLookupServiceImpl(mockVehiclesLookupService)
//      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
//      implicit val config: Config = mock[Config]
//      val vehiclesLookup = new vrm_assign.VehicleAndKeeperLookup(
//        bruteForceServiceImpl(permitted = true),
//        vehicleAndKeeperLookupServiceImpl)
//      val result = vehiclesLookup.submit(request)
//
//      whenReady(result) {
//        r =>
//          val trackingIdCaptor = ArgumentCaptor.forClass(classOf[String])
//          verify(mockVehiclesLookupService).callVehicleAndKeeperLookupService(any[VehicleDetailsRequest], trackingIdCaptor.capture())
//          trackingIdCaptor.getValue should be(ClearTextClientSideSessionFactory.DefaultTrackingId)
//      }
//    }

    "call audit service with 'default_test_tracking_id' when DocRefNumberNotLatest and no transaction id cookie exists" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val (vehicleLookup, dateService, auditService) = vehicleLookupAndAuditStubs(vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseDocRefNumberNotLatest)
      val expected = new AuditMessage(
        name = "VehicleLookupToVehicleLookupFailure",
        serviceType = "PR Assign",
        ("transactionId",ClearTextClientSideSessionFactory.DefaultTrackingId),
        ("timestamp",dateService.dateTimeISOChronology),
        ("rejectionCode",RecordMismatch)
      )
      val result = vehicleLookup.submit(request)

      whenReady(result) { r =>
        verify(auditService, times(1)).send(expected)
      }
    }

    "call audit service with 'default_test_tracking_id' when Postcodes don't match and no transaction id cookie exists" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val (vehicleLookup, dateService, auditService) = vehicleLookupAndAuditStubs()
      val expected = new AuditMessage(
        name = "VehicleLookupToVehicleLookupFailure",
        serviceType = "PR Assign",
        ("transactionId",ClearTextClientSideSessionFactory.DefaultTrackingId),
        ("timestamp",dateService.dateTimeISOChronology),
        ("rejectionCode","PR002 - vehicle_and_keeper_lookup_keeper_postcode_mismatch")
      )
      val result = vehicleLookup.submit(request)

      whenReady(result) { r =>
          verify(auditService, times(1)).send(expected)
      }
    }
  }

  "back" should {

    "redirect to Before You Start page when back button is pressed" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody()
      val result = vehicleLookupStubs().back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
    }
  }

  private lazy val present = {
    val request = FakeRequest()
    vehicleLookupStubs().present(request)
  }

  private def vehicleLookupStubs(isPrototypeBannerVisible: Boolean = true,
                                 permitted: Boolean = true,
                                 vehicleAndKeeperLookupStatusAndResponse: (Int, Option[VehicleAndKeeperDetailsResponse]) = vehicleAndKeeperDetailsResponseSuccess) = {
    testInjector(
      new TestBruteForcePreventionWebService(permitted = permitted),
      new TestConfig(isPrototypeBannerVisible = isPrototypeBannerVisible),
      new TestVehicleAndKeeperLookupWebService(statusAndResponse = vehicleAndKeeperLookupStatusAndResponse),
      new TestAuditService(),
      new TestDateService()
    ).
      getInstance(classOf[VehicleLookup])
  }

  private def vehicleLookupAndAuditStubs(isPrototypeBannerVisible: Boolean = true,
                                 permitted: Boolean = true,
                                 vehicleAndKeeperLookupStatusAndResponse: (Int, Option[VehicleAndKeeperDetailsResponse]) = vehicleAndKeeperDetailsResponseSuccess
                                 ) = {
    val auditService = mock[AuditService]
    val ioc = testInjector(
      new TestBruteForcePreventionWebService(permitted = permitted),
      new TestConfig(isPrototypeBannerVisible = isPrototypeBannerVisible),
      new TestVehicleAndKeeperLookupWebService(statusAndResponse = vehicleAndKeeperLookupStatusAndResponse),
      new TestAuditService(auditService),
      new TestDateService()
    )
    (ioc.getInstance(classOf[VehicleLookup]), ioc.getInstance(classOf[DateService]), ioc.getInstance(classOf[AuditService]))
  }

  private def buildCorrectlyPopulatedRequest(referenceNumber: String = ReferenceNumberValid,
                                             registrationNumber: String = RegistrationNumberValid,
                                             postcode: String = PostcodeValid,
                                             KeeperConsent: String = KeeperConsentValid) = {
    FakeRequest().withFormUrlEncodedBody(
      DocumentReferenceNumberId -> referenceNumber,
      VehicleRegistrationNumberId -> registrationNumber,
      PostcodeId -> postcode,
      KeeperConsentId -> KeeperConsent)
  }

  private def vehicleAndKeeperLookupCallFails(isPrototypeBannerVisible: Boolean = true,
                                              permitted: Boolean = true) = {
    testInjector(
      new TestBruteForcePreventionWebService(permitted = permitted),
      new TestConfig(isPrototypeBannerVisible = isPrototypeBannerVisible),
      new VehicleAndKeeperLookupCallFails()
    ).
      getInstance(classOf[VehicleLookup])
  }

  private def vehicleAndKeeperDetailsCallNoResponse(isPrototypeBannerVisible: Boolean = true,
                                                    permitted: Boolean = true) = {
    testInjector(
      new TestBruteForcePreventionWebService(permitted = permitted),
      new TestConfig(isPrototypeBannerVisible = isPrototypeBannerVisible),
      new VehicleAndKeeperLookupCallNoResponse()
    ).
      getInstance(classOf[VehicleLookup])
  }

  private def vehicleAndKeeperDetailsCallServerDown(isPrototypeBannerVisible: Boolean = true,
                                                    permitted: Boolean = true) = {
    testInjector(
      new TestBruteForcePreventionWebService(permitted = permitted),
      new TestConfig(isPrototypeBannerVisible = isPrototypeBannerVisible),
      new VehicleAndKeeperDetailsCallServerDown()
    ).
      getInstance(classOf[VehicleLookup])
  }

  private def vehicleAndKeeperDetailsCallDocRefNumberNotLatest(isPrototypeBannerVisible: Boolean = true,
                                                               permitted: Boolean = true) = {
    testInjector(
      new TestBruteForcePreventionWebService(permitted = permitted),
      new TestConfig(isPrototypeBannerVisible = isPrototypeBannerVisible),
      new VehicleAndKeeperDetailsCallDocRefNumberNotLatest()
    ).
      getInstance(classOf[VehicleLookup])
  }

  private def vehicleAndKeeperDetailsCallVRMNotFound(isPrototypeBannerVisible: Boolean = true,
                                                     permitted: Boolean = true) = {
    testInjector(
      new TestBruteForcePreventionWebService(permitted = permitted),
      new TestConfig(isPrototypeBannerVisible = isPrototypeBannerVisible),
      new VehicleAndKeeperDetailsCallVRMNotFound()
    ).
      getInstance(classOf[VehicleLookup])
  }
}
