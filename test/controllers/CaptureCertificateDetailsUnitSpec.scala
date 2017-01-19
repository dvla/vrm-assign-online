package controllers

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vrmassigneligibility.VrmAssignEligibilityCallDirectToPaperError
import composition.webserviceclients.vrmassigneligibility.VrmAssignEligibilityCallNotEligibleError
import composition.webserviceclients.vrmassigneligibility.VrmAssignEligibilityCallExpiredCertWithin6Years
import composition.webserviceclients.vrmassigneligibility.VrmAssignEligibilityCallExpiredCertOver6Years
import helpers.{UnitSpec, TestWithApplication}
import helpers.vrm_assign.CookieFactoryForUnitSpecs.captureCertificateDetailsFormModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.captureCertificateDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.trackingIdModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.Certificate.{Expired, ExpiredWithFee, Unknown, Valid}
import org.joda.time.{DateTime, Days, Years}
import org.joda.time.format.DateTimeFormat
import org.mockito.Mockito.{times, verify}
import pages.vrm_assign.ConfirmBusinessPage
import pages.vrm_assign.ConfirmPage
import pages.vrm_assign.LeaveFeedbackPage
import pages.vrm_assign.VehicleLookupFailurePage
import pages.vrm_assign.VehicleLookupPage
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.{ClearTextClientSideSessionFactory, TrackingId}
import common.model.MicroserviceResponseModel
import common.model.MicroserviceResponseModel.MsResponseCacheKey
import common.services.DateService
import common.testhelpers.CookieHelper.fetchCookiesFromHeaders
import common.testhelpers.JsonUtils.deserializeJsonToModel
import views.vrm_assign.CaptureCertificateDetails.CaptureCertificateDetailsCacheKey
import views.vrm_assign.CaptureCertificateDetails.CaptureCertificateDetailsFormModelCacheKey
import views.vrm_assign.CaptureCertificateDetails.CertificateDateId
import views.vrm_assign.CaptureCertificateDetails.CertificateDocumentCountId
import views.vrm_assign.CaptureCertificateDetails.CertificateRegistrationMarkId
import views.vrm_assign.CaptureCertificateDetails.CertificateTimeId
import webserviceclients.audit2.AuditRequest
import webserviceclients.audit2.AuditService
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateDateValid
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateDocumentCountValid
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateTimeValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.BusinessConsentValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperConsentValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.TransactionIdValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.VehicleMakeValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.VehicleModelValid

class CaptureCertificateDetailsUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperDetailsModel(),
          vehicleAndKeeperLookupFormModel()
        )
      val (captureCertificateDetails, dateService, auditService) = build()
      var result = captureCertificateDetails.present(request)

      whenReady(result) { result =>
        result.header.status should equal(OK)
      }
    }
  }

  "submit" should {
    "redirect back to Error page if required cookies do not exist" in new TestWithApplication {
      val request = FakeRequest()
      val (captureCertificateDetails, dateService, auditService) = build()
      val result = captureCertificateDetails.submit(request)
      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) match {
            case Some(url) => url should include(routes.Error.present("user went to CaptureCertificateDetails " +
              "submit without the VehicleAndKeeperDetailsModel cookie").url)
            case _ => fail("did not redirect to the error page")
          }
      }
    }

    "redirect to confirm page when the form is completed successfully" in new TestWithApplication {
      val (captureCertificateDetails, _, _) = build()
      val result = captureCertificateDetails.submit(requestWithCookies)
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
              model.certificateRegistrationMark should equal(RegistrationNumberValid.toUpperCase)
              model.certificateTime should equal(CertificateTimeValid.toUpperCase)
            case None => fail(s"$cookieName cookie not found")
          }
      }
    }

    "redirect to confirm page when the form is completed successfully " +
      "and expired certificate is within 6 years" in new TestWithApplication {
      val (captureCertificateDetails, _, _) = build()
      val result = captureCertificateDetails.submit(requestWithCookies)
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
              model.certificateRegistrationMark should equal(RegistrationNumberValid.toUpperCase)
              model.certificateTime should equal(CertificateTimeValid.toUpperCase)
            case None => fail(s"$cookieName cookie not found")
          }
      }
    }

    "redirect to vehicles failure page when the form is completed successfully " +
      "but fails eligibility with a direct to paper code" in new TestWithApplication {
      val (captureCertificateDetails, dateService, auditService) = buildWithDirectToPaper()
      val result = captureCertificateDetails.submit(requestWithCookies)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to vehicles failure page when the form is completed successfully " +
      "but fails eligibility with a not eligible code" in new TestWithApplication {
      val (captureCertificateDetails, dateService, auditService) = buildWithNotEligible()
      val result = captureCertificateDetails.submit(requestWithCookies)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to vehicles failure page when the form is completed successfully " +
      "but certificate expiry is over 6 years" in new TestWithApplication {
      val (captureCertificateDetails, dateService, auditService) =
        buildWithModule(() => Seq(new VrmAssignEligibilityCallExpiredCertOver6Years, MockedDateService))
      val result = captureCertificateDetails.submit(requestWithCookies)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
        val cookies = fetchCookiesFromHeaders(r)
        val cookieName = MsResponseCacheKey
        cookies.find(_.name == cookieName) match {
          case Some(cookie) =>
            val json = cookie.value
            val model = deserializeJsonToModel[MicroserviceResponseModel](json)
            model.msResponse.message should equal("vrm_assign_eligibility_cert_expired")
          case None => fail(s"$cookieName cookie not found")
        }
      }
    }
  }

  "exit" should {
    "redirect to LeaveFeedback" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperDetailsModel()
        )
      val (captureCertificateDetails, dateService, auditService) = build()
      val result = captureCertificateDetails.exit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(LeaveFeedbackPage.address))
      }
    }

    "call audit service once with 'default_test_tracking_id' when the required cookies exist" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperDetailsModel(),
          trackingIdModel()
        )
      val (captureCertificateDetails, dateService, auditService) = build()
      val expected = new AuditRequest(
        name = AuditRequest.CaptureCertificateDetailsToExit,
        serviceType = "PR Assign",
        data = Seq(
          ("transactionId", ClearTextClientSideSessionFactory.DefaultTrackingId.value),
          ("timestamp", dateService.dateTimeISOChronology),
          ("currentVrm", RegistrationNumberValid),
          ("make", VehicleMakeValid.get),
          ("model", VehicleModelValid.get),
          ("keeperName", "MR DAVID JONES"),
          ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA")
        )
      )

      val result = captureCertificateDetails.exit(request)

      whenReady(result) { r =>
        verify(auditService, times(1)).send(expected, TrackingId("trackingId"))
      }
    }

    "call audit service once with expected values when the required cookies exist" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperDetailsModel(),
          transactionId(),
          trackingIdModel()
        )
      val (captureCertificateDetails, dateService, auditService) = build()
      val expected = new AuditRequest(
        name = AuditRequest.CaptureCertificateDetailsToExit,
        serviceType = "PR Assign",
        data = Seq(
          ("transactionId", TransactionIdValid),
          ("timestamp", dateService.dateTimeISOChronology),
          ("currentVrm", RegistrationNumberValid),
          ("make", VehicleMakeValid.get),
          ("model", VehicleModelValid.get),
          ("keeperName", "MR DAVID JONES"),
          ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA")
        )
      )

      val result = captureCertificateDetails.exit(request)
      whenReady(result) { r =>
        verify(auditService, times(1)).send(expected, TrackingId("trackingId"))
      }
    }
  }

  "back" should {
    "redirect to Vehicle Lookup page when the user is a keeper" in new TestWithApplication {
      whenReady(back(KeeperConsentValid)) { r =>
        r.header.status should equal(SEE_OTHER)
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to Confirm Business page when the user is a business" in new TestWithApplication {
      whenReady(back(BusinessConsentValid)) { r =>
        r.header.status should equal(SEE_OTHER)
        r.header.headers.get(LOCATION) should equal(Some(ConfirmBusinessPage.address))
      }
    }
  }

  "validateCertificate" should {
    "allow payment if cert expiry date is within 6 years" in new TestWithApplication {
      val (captureCertificateDetails, _, _) = buildWithModule(() => Seq(MockedDateService))

      val expiryDate = Some(DateTime.now.minusYears(6))
      captureCertificateDetails.validateCertificate(expiryDate) shouldBe a [ExpiredWithFee]
    }

    "not allow payment if cert expiry date is over 6 years" in new TestWithApplication {
      val (captureCertificateDetails, _, _) = buildWithModule(() => Seq(MockedDateService))

      val expiryDate = Some(DateTime.now.minusYears(6).minusDays(1))
      captureCertificateDetails.validateCertificate(expiryDate) shouldBe a [Expired]
    }

    "not calculate a payment if cert hasn't expired" in new TestWithApplication {
      val (captureCertificateDetails, _, _) = buildWithModule(() => Seq(MockedDateService))

      val expiryDate = Some(DateTime.now.plusYears(6))
      captureCertificateDetails.validateCertificate(expiryDate) shouldBe a [Valid]
    }

    "not require payment if no cert date" in new TestWithApplication {
      val (captureCertificateDetails, _, _) = buildWithModule(() => Seq(MockedDateService))

      captureCertificateDetails.validateCertificate(None) should equal(Unknown)
    }

  }

  private def back(keeperConsent: String) = {
    val request = FakeRequest()
      .withCookies(
        vehicleAndKeeperLookupFormModel(keeperConsent = keeperConsent),
        vehicleAndKeeperDetailsModel()
      )
    val (captureCertificateDetails, _, _) = build()
    captureCertificateDetails.back(request)
  }

  private def build() = {
    val ioc = testInjector()
    (
      ioc.getInstance(classOf[CaptureCertificateDetails]),
      ioc.getInstance(classOf[DateService]),
      ioc.getInstance(classOf[AuditService])
    )
  }

  private def buildWithModule(err: () => Seq[com.google.inject.Module]) = {
    val ioc = testInjector(
      err():_*
    )
    (
      ioc.getInstance(classOf[CaptureCertificateDetails]),
      ioc.getInstance(classOf[DateService]),
      ioc.getInstance(classOf[AuditService])
    )
  }

  private def buildWithDirectToPaper() =
    buildWithModule(() => Seq(new VrmAssignEligibilityCallDirectToPaperError))

  private def buildWithNotEligible() =
    buildWithModule(() => Seq(new VrmAssignEligibilityCallNotEligibleError))

  private val MockedDateService = {
    val d = mock[DateService]
    org.mockito.Mockito.when(d.now).thenReturn(DateTime.now.toInstant)
    new ScalaModule() {
      override def configure() = bind[DateService].toInstance(d)
    }
  }

  private lazy val requestWithCookies = buildCorrectlyPopulatedRequest()
    .withCookies(vehicleAndKeeperDetailsModel())
    .withCookies(vehicleAndKeeperLookupFormModel(replacementVRN = RegistrationNumberValid))
    .withCookies(captureCertificateDetailsFormModel())
    .withCookies(captureCertificateDetailsModel())

  private def buildCorrectlyPopulatedRequest(certificateDate: String = CertificateDateValid,
                                             certificateDocumentCount: String = CertificateDocumentCountValid,
                                             certificateRegistrationMark: String = RegistrationNumberValid,
                                             certificateTime: String = CertificateTimeValid) = {
    FakeRequest().withFormUrlEncodedBody(
      CertificateDateId -> certificateDate,
      CertificateDocumentCountId -> certificateDocumentCount,
      CertificateRegistrationMarkId -> certificateRegistrationMark,
      CertificateTimeId -> certificateTime)
  }
}
