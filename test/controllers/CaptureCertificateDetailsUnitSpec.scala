package controllers

import audit1.{AuditMessage, AuditService}
import composition.audit1Mock.MockAuditLocalService
import composition.audit2.AuditServiceDoesNothing
import composition.vrmassigneligibility.{TestVrmAssignEligibilityWebService, VrmAssignEligibilityCallDirectToPaperError, VrmAssignEligibilityCallNotEligibleError}
import composition.{TestBruteForcePreventionWebService, TestDateService, WithApplication}
import helpers.JsonUtils.deserializeJsonToModel
import helpers.UnitSpec
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_assign.CookieFactoryForUnitSpecs._
import models.{CaptureCertificateDetailsModel, CaptureCertificateDetailsFormModel}
import org.mockito.Mockito._
import pages.vrm_assign.{ConfirmPage, LeaveFeedbackPage, MicroServiceErrorPage, VehicleLookupFailurePage}
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import views.vrm_assign.CaptureCertificateDetails._
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants._
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._
import webserviceclients.vrmretentioneligibility.VrmAssignEligibilityWebService

final class CaptureCertificateDetailsUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperDetailsModel()
        )
      val (captureCertificateDetails, dateService, auditService) = checkEligibility()

      var result = captureCertificateDetails.present(request)

      whenReady(result, timeout) { result =>
        result.header.status should equal(OK)
      }
    }
  }

  "submit" should {

    "redirect to MicroServiceError page is required cookies do not exist" in new WithApplication {
      val request = FakeRequest()
      val (captureCertificateDetails, dateService, auditService) = checkEligibility()
      val result = captureCertificateDetails.submit(request)
      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to confirm page when the form is completed successfully" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(vehicleAndKeeperDetailsModel())
        .withCookies(vehicleAndKeeperLookupFormModel())
        .withCookies(captureCertificateDetailsFormModel())
        .withCookies(captureCertificateDetailsModel())
      val (captureCertificateDetails, dateService, auditService) = checkEligibility()
      val result = captureCertificateDetails.submit(request)
      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(ConfirmPage.address))
          val cookies = fetchCookiesFromHeaders(r)
          val cookieName = CaptureCertificateDetailsFormModelCacheKey
          cookies.find(_.name == cookieName) match {
            case Some(cookie) =>
              val json = cookie.value
              val model = deserializeJsonToModel[CaptureCertificateDetailsFormModel](json)
              model.certificateDate should equal(CertificateDateValid.toUpperCase)
              model.certificateDocumentCount should equal(CertificateDocumentCountValid.toUpperCase)
              model.certificateRegistrationMark should equal(CertificateRegistrationMarkValid.toUpperCase)
              model.certificateTime should equal(CertificateTimeValid.toUpperCase)
              model.prVrm should equal(PrVrmValid.toUpperCase)
            case None => fail(s"$cookieName cookie not found")
          }
      }
    }

    "redirect to vehicles failure page when the form is completed successfully but fails eligibility with a direct to paper code" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(vehicleAndKeeperDetailsModel())
        .withCookies(vehicleAndKeeperLookupFormModel())
        .withCookies(captureCertificateDetailsFormModel())
        .withCookies(captureCertificateDetailsModel())
      val (captureCertificateDetails, dateService, auditService) = checkEligibilityDirectToPaper()
      val result = captureCertificateDetails.submit(request)
      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
          val cookies = fetchCookiesFromHeaders(r)
          val cookieName = CaptureCertificateDetailsCacheKey
          cookies.find(_.name == cookieName) match {
            case Some(cookie) =>
              val json = cookie.value
              val model = deserializeJsonToModel[CaptureCertificateDetailsModel](json)
              model.outstandingDates.size should equal(2)
            case None => fail(s"$cookieName cookie not found")
          }
      }
    }

    "redirect to vehicles failure page when the form is completed successfully but fails eligibility with a not eligible code" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(vehicleAndKeeperDetailsModel())
        .withCookies(vehicleAndKeeperLookupFormModel())
        .withCookies(captureCertificateDetailsFormModel())
        .withCookies(captureCertificateDetailsModel())
      val (captureCertificateDetails, dateService, auditService) = checkEligibilityNotEligible()
      val result = captureCertificateDetails.submit(request)
      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
          val cookies = fetchCookiesFromHeaders(r)
          val cookieName = CaptureCertificateDetailsCacheKey
          cookies.find(_.name == cookieName) match {
            case Some(cookie) =>
              val json = cookie.value
              val model = deserializeJsonToModel[CaptureCertificateDetailsModel](json)
              model.outstandingDates.size should equal(0)
            case None => fail(s"$cookieName cookie not found")
          }
      }
    }

  }

  "exit" should {

    "redirect to LeaveFeedback" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperDetailsModel()
        )
      val (captureCertificateDetails, dateService, auditService) = checkEligibility()

      val result = captureCertificateDetails.exit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(LeaveFeedbackPage.address))
      }
    }

    "call audit service once with 'default_test_tracking_id' when the required cookies exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperDetailsModel()
        )
      val (captureCertificateDetails, dateService, auditService) = checkEligibility()
      val expected = new AuditMessage(
        name = AuditMessage.CaptureCertificateDetailsToExit,
        serviceType = "PR Assign",
        ("transactionId", ClearTextClientSideSessionFactory.DefaultTrackingId),
        ("timestamp", dateService.dateTimeISOChronology),
        ("currentVrm", RegistrationNumberValid),
        ("make", VehicleMakeValid.get),
        ("model", VehicleModelValid.get),
        ("keeperName", "MR DAVID JONES"),
        ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA")
      )

      val result = captureCertificateDetails.exit(request)

      whenReady(result, timeout) { r =>
        verify(auditService, times(1)).send(expected)
      }
    }

    "call audit service once with expected values when the required cookies exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperDetailsModel(),
          transactionId()
        )
      val (captureCertificateDetails, dateService, auditService) = checkEligibility()
      val expected = new AuditMessage(
        name = AuditMessage.CaptureCertificateDetailsToExit,
        serviceType = "PR Assign",
        ("transactionId", TransactionIdValid),
        ("timestamp", dateService.dateTimeISOChronology),
        ("currentVrm", RegistrationNumberValid),
        ("make", VehicleMakeValid.get),
        ("model", VehicleModelValid.get),
        ("keeperName", "MR DAVID JONES"),
        ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA")
      )

      val result = captureCertificateDetails.exit(request)

      whenReady(result, timeout) { r =>
        verify(auditService, times(1)).send(expected)
      }
    }
  }

  private def checkEligibility() = {
    val auditService1 = mock[AuditService]
    val ioc = testInjector(
      new TestBruteForcePreventionWebService(permitted = true),
      new TestDateService(),
      new MockAuditLocalService(auditService1),
      new AuditServiceDoesNothing,
      new TestVrmAssignEligibilityWebService()//(vrmAssignEligibilityWebService = mock[VrmAssignEligibilityWebService])
    )
    (ioc.getInstance(classOf[CaptureCertificateDetails]), ioc.getInstance(classOf[DateService]), ioc.getInstance(classOf[AuditService]))
  }

  private def checkEligibilityDirectToPaper() = {
    val auditService1 = mock[AuditService]
    val ioc = testInjector(
      new TestBruteForcePreventionWebService(permitted = true),
      new TestDateService(),
      new MockAuditLocalService(auditService1),
      new AuditServiceDoesNothing,
      new VrmAssignEligibilityCallDirectToPaperError
    )
    (ioc.getInstance(classOf[CaptureCertificateDetails]), ioc.getInstance(classOf[DateService]), ioc.getInstance(classOf[AuditService]))
  }

  private def checkEligibilityNotEligible() = {
    val auditService1 = mock[AuditService]
    val ioc = testInjector(
      new TestBruteForcePreventionWebService(permitted = true),
      new TestDateService(),
      new MockAuditLocalService(auditService1),
      new AuditServiceDoesNothing,
      new VrmAssignEligibilityCallNotEligibleError
    )
    (ioc.getInstance(classOf[CaptureCertificateDetails]), ioc.getInstance(classOf[DateService]), ioc.getInstance(classOf[AuditService]))
  }

  private def buildCorrectlyPopulatedRequest(certificateDate: String = CertificateDateValid,
                                             certificateDocumentCount: String = CertificateDocumentCountValid,
                                             certificateRegistrationMark: String = CertificateRegistrationMarkValid,
                                             certificateTime: String = CertificateTimeValid,
                                             prVrm: String = PrVrmValid) = {
    FakeRequest().withFormUrlEncodedBody(
      CertificateDateId -> certificateDate,
      CertificateDocumentCountId -> certificateDocumentCount,
      CertificateRegistrationMarkId -> certificateRegistrationMark,
      CertificateTimeId -> certificateTime,
      PrVrmId -> prVrm)
  }

}