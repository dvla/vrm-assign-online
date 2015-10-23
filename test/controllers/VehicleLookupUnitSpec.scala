package controllers

import composition.TestConfig
import composition.webserviceclients.bruteforceprevention.TestBruteForcePreventionWebServiceBinding
import composition.webserviceclients.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebServiceBinding
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsCallDocRefNumberNotLatest
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsCallServerDown
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsCallVRMNotFound
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupCallNoResponse
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupCallFails
import controllers.Common.PrototypeHtml
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.JsonUtils.deserializeJsonToModel
import helpers.UnitSpec
import helpers.vrm_assign.CookieFactoryForUnitSpecs
import helpers.WithApplication
import models.CacheKeyPrefix
import models.VehicleAndKeeperLookupFormModel
import org.mockito.Mockito.verify
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.CaptureCertificateDetailsPage
import pages.vrm_assign.MicroServiceErrorPage
import pages.vrm_assign.VehicleLookupFailurePage
import pages.vrm_assign.VrmLockedPage
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.mappings.DocumentReferenceNumber
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.bruteForcePreventionViewModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel.vehicleAndKeeperLookupDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.DmsWebHeaderDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup
import vehicleandkeeperlookup.VehicleAndKeeperLookupFailureResponse
import vehicleandkeeperlookup.VehicleAndKeeperLookupRequest
import vehicleandkeeperlookup.VehicleAndKeeperLookupSuccessResponse
import vehicleandkeeperlookup.VehicleAndKeeperLookupWebService
import views.vrm_assign.Payment.PaymentTransNoCacheKey
import views.vrm_assign.VehicleLookup.DocumentReferenceNumberId
import views.vrm_assign.VehicleLookup.KeeperConsentId
import views.vrm_assign.VehicleLookup.PostcodeId
import views.vrm_assign.VehicleLookup.{ReplacementVRN => ReplacementVRNForm}
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey
import views.vrm_assign.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey
import views.vrm_assign.VehicleLookup.VehicleAndKeeperLookupResponseCodeCacheKey
import views.vrm_assign.VehicleLookup.VehicleRegistrationNumberId
import webserviceclients.fakes.AddressLookupServiceConstants.PostcodeValid
import webserviceclients.fakes.BruteForcePreventionWebServiceConstants
import webserviceclients.fakes.BruteForcePreventionWebServiceConstants.VrmLocked
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperConsentValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperPostcodeValidForMicroService
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.ReferenceNumberValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.ReplacementVRN
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberWithSpaceValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseDocRefNumberNotLatest
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseNotFoundResponseCode
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseSuccess
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseVRMNotFound
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsServerDown

class VehicleLookupUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      present.futureValue.header.status should equal(play.api.http.Status.OK)
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
      val result = vehicleLookupStubsPrototypeBannerNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {
    "redirect to CaptureCertificateDetailsPage after a valid submit and " +
      "true message returned from the fake microservice" in new WithApplication {
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

          val cookie2Name = vehicleAndKeeperLookupDetailsCacheKey
          cookies.find(_.name == cookie2Name) match {
            case Some(cookie) =>
              val json = cookie.value
              deserializeJsonToModel[VehicleAndKeeperDetailsModel](json)
            case None => fail(s"$cookie2Name cookie not found")
          }
      }
    }

    "remove spaces from registrationNumber" in new WithApplication {
      // DE7 Spaces should be stripped
      val request = buildCorrectlyPopulatedRequest(registrationNumber = RegistrationNumberWithSpaceValid)
      val result = vehicleLookupStubs().submit(request)

      whenReady(result) {
        r =>
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should contain(VehicleAndKeeperLookupFormModelCacheKey)
      }
    }

    "redirect to MicroServiceError after a submit and no response code and no " +
      "vehicledetailsdto returned from the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseNotFoundResponseCode
      ).submit(request)

      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and vrm not found " +
      "the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseVRMNotFound
      ).submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
    }

      "redirect to VehicleAndKeeperLookupFailure after a submit and document reference number " +
        "mismatch returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseDocRefNumberNotLatest
      ).submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and vss error returned " +
      "by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsServerDown
      ).submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    }

    "replace max length error message for document reference number with "+
      "standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "1" * (DocumentReferenceNumber.MaxLength + 1))
      val result = vehicleLookupStubs().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number"
        .r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number"
        .r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace required and min length error messages for document reference number with " +
      "standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "")
      val result = vehicleLookupStubs().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number"
        .r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number"
        .r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace max length error message for vehicle registration mark with " +
      "standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "PJ05YYYX")
      val result = vehicleLookupStubs().submit(request)
      val count = "Must be valid registration number".r.findAllIn(contentAsString(result)).length

      count should equal(2)
    }

    "replace required and min length error messages for vehicle registration mark with " +
      "standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "")
      val result = vehicleLookupStubs().submit(request)
      val count = "Must be valid registration number".r.findAllIn(contentAsString(result)).length
      // The same message is displayed in 2 places -
      // once in the validation-summary at the top of the page
      // and once above the field.
      count should equal(2)
    }

    "redirect to MicroserviceError page when vehicleAndKeeperLookup throws an exception" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperLookupCallFails.submit(request)

      whenReady(result, timeout) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "does not write VehicleAndKeeperDetailsModel cookie when microservice throws an exception" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperLookupCallFails.submit(request)

      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should not contain vehicleAndKeeperLookupDetailsCacheKey
      }
    }

    "redirect to MicroServiceError after a submit " +
      "if response status is Ok and no response payload" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallNoResponse.submit(request)

      whenReady(result, timeout) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "write cookie when vss error returned by the microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallServerDown.submit(request)

      whenReady(result) {
        r =>
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should contain(VehicleAndKeeperLookupFormModelCacheKey)
      }
    }

    "write cookie when vrm not found by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallVRMNotFound.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(
          PaymentTransNoCacheKey, TransactionIdCacheKey, bruteForcePreventionViewModelCacheKey,
          VehicleAndKeeperLookupResponseCodeCacheKey, VehicleAndKeeperLookupFormModelCacheKey)
      }
    }

    "redirect to vrm locked when valid submit and brute force prevention returns not permitted" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = VrmLocked)
      val result = vehicleLookupStubsBruteForceReturnsNotPermitted.submit(request)
      result.futureValue.header.headers.get(LOCATION) should equal(Some(VrmLockedPage.address))
    }

    "redirect to VehicleAndKeeperLookupFailure and display 1st attempt message when " +
      "document reference number not found and security service returns 1st attempt" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallDocRefNumberNotLatest.submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
    }

    "write cookie when document reference number mismatch returned by microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallDocRefNumberNotLatest.submit(request)
      whenReady(result) {
        r =>
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should contain allOf(
            bruteForcePreventionViewModelCacheKey,
            VehicleAndKeeperLookupResponseCodeCacheKey,
            VehicleAndKeeperLookupFormModelCacheKey
            )
      }
    }

    "redirect to VehicleAndKeeperLookupFailure and display 2nd attempt message when " +
      "document reference number not found and security service returns 2nd attempt" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(
        registrationNumber = BruteForcePreventionWebServiceConstants.VrmAttempt2
      )
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseDocRefNumberNotLatest
      ).submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
    }

    "send a request and a trackingId to the vehicleAndKeeperLookupWebService" in new WithApplication {
      val trackingId = TrackingId("default_test_tracking_id")
      val vehicleAndKeeperLookupWebService = mock[VehicleAndKeeperLookupWebService]
      val request = buildCorrectlyPopulatedRequest(postcode = KeeperPostcodeValidForMicroService)
        .withCookies(CookieFactoryForUnitSpecs.trackingIdModel(trackingId))
      val (vehicleLookup, dateService) = vehicleLookupStubs(
        vehicleAndKeeperLookupWebService = vehicleAndKeeperLookupWebService
      )
      val result = vehicleLookup.submit(request)

      whenReady(result, timeout) {
        r =>
          val expectedRequest = VehicleAndKeeperLookupRequest(
            dmsHeader = buildHeader(trackingId, dateService),
            referenceNumber = ReferenceNumberValid,
            registrationNumber = RegistrationNumberValid,
            transactionTimestamp = dateService.now.toDateTime
          )
          verify(vehicleAndKeeperLookupWebService).invoke(request = expectedRequest, trackingId = trackingId)
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

  private def present = {
    val request = FakeRequest()
    vehicleLookupStubs().present(request)
  }

  private def vehicleLookupStubs(vehicleAndKeeperLookupStatusAndResponse:
                                 (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                   VehicleAndKeeperLookupSuccessResponse]]) = vehicleAndKeeperDetailsResponseSuccess) = {
    testInjector(
      new TestVehicleAndKeeperLookupWebServiceBinding(statusAndResponse = vehicleAndKeeperLookupStatusAndResponse)
    ).getInstance(classOf[VehicleLookup])
  }

  private def vehicleLookupStubsPrototypeBannerNotVisible = {
    testInjector(
      new TestConfig(isPrototypeBannerVisible = false)
    ).getInstance(classOf[VehicleLookup])
  }

  private def vehicleLookupStubsBruteForceReturnsNotPermitted = {
    testInjector(
      new TestBruteForcePreventionWebServiceBinding(permitted = false)
    ).getInstance(classOf[VehicleLookup])
  }

  private def vehicleLookupStubs(vehicleAndKeeperLookupWebService: VehicleAndKeeperLookupWebService) = {
    val injector = testInjector(
      new TestVehicleAndKeeperLookupWebServiceBinding(
        vehicleAndKeeperLookupWebService = vehicleAndKeeperLookupWebService
      )
    )
    (injector.getInstance(classOf[VehicleLookup]), injector.getInstance(classOf[DateService]))
  }

  private def buildCorrectlyPopulatedRequest(replacementVRN: String = ReplacementVRN,
                                             referenceNumber: String = ReferenceNumberValid,
                                             registrationNumber: String = RegistrationNumberValid,
                                             postcode: String = PostcodeValid,
                                             KeeperConsent: String = KeeperConsentValid) = {
    FakeRequest().withFormUrlEncodedBody(
      ReplacementVRNForm -> replacementVRN,
      DocumentReferenceNumberId -> referenceNumber,
      VehicleRegistrationNumberId -> registrationNumber,
      PostcodeId -> postcode,
      KeeperConsentId -> KeeperConsent)
  }

  private def vehicleAndKeeperLookupCallFails = {
    testInjector(
      new VehicleAndKeeperLookupCallFails()
    ).getInstance(classOf[VehicleLookup])
  }

  private def vehicleAndKeeperDetailsCallNoResponse = {
    testInjector(
      new VehicleAndKeeperLookupCallNoResponse()
    ).getInstance(classOf[VehicleLookup])
  }

  private def vehicleAndKeeperDetailsCallServerDown = {
    testInjector(
      new VehicleAndKeeperDetailsCallServerDown()
    ).getInstance(classOf[VehicleLookup])
  }

  private def vehicleAndKeeperDetailsCallDocRefNumberNotLatest = {
    testInjector(
      new VehicleAndKeeperDetailsCallDocRefNumberNotLatest()
    ).getInstance(classOf[VehicleLookup])
  }

  private def vehicleAndKeeperDetailsCallVRMNotFound = {
    testInjector(
      new VehicleAndKeeperDetailsCallVRMNotFound()
    ).getInstance(classOf[VehicleLookup])
  }

  private def buildHeader(trackingId: TrackingId, dateService: DateService): DmsWebHeaderDto = {
    val alwaysLog = true
    val englishLanguage = "EN"
    DmsWebHeaderDto(conversationId = trackingId.value,
      originDateTime = dateService.now.toDateTime,
      applicationCode = "test-applicationCode",
      channelCode = "test-channelCode",
      contactId = 42,
      eventFlag = alwaysLog,
      serviceTypeCode = "test-dmsServiceTypeCode",
      languageCode = englishLanguage,
      endUser = None)
  }
}
