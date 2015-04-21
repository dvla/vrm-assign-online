package controllers

import composition.WithApplication
import composition.webserviceclients.vrmassigneligibility.{VrmAssignEligibilityCallDirectToPaperError, VrmAssignEligibilityCallNotEligibleError}
import helpers.JsonUtils.deserializeJsonToModel
import helpers.UnitSpec
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_assign.CookieFactoryForUnitSpecs._
import models.{CaptureCertificateDetailsFormModel, CaptureCertificateDetailsModel}
import org.mockito.Mockito._
import pages.vrm_assign.ConfirmBusinessPage
import pages.vrm_assign.ErrorPage
import pages.vrm_assign.VehicleLookupPage
import pages.vrm_assign.{ConfirmPage, LeaveFeedbackPage, VehicleLookupFailurePage}
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import views.vrm_assign.CaptureCertificateDetails._
import webserviceclients.audit2.AuditRequest
import webserviceclients.audit2.AuditService
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants._
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._

final class CaptureCertificateDetailsUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperDetailsModel(),
          vehicleAndKeeperLookupFormModel()
        )
      val (captureCertificateDetails, dateService, auditService) = build()

      var result = captureCertificateDetails.present(request)

      whenReady(result, timeout) { result =>
        result.header.status should equal(OK)
      }
    }
  }

  "submit" should {

    "redirect back to Error page is required cookies do not exist" in new WithApplication {
      val request = FakeRequest()
      val (captureCertificateDetails, dateService, auditService) = build()
      val result = captureCertificateDetails.submit(request)
      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) match {
            case Some(url) => url should include(routes.Error.present("user went to CaptureCertificateDetails submit without the VehicleAndKeeperDetailsModel cookie").url)
            case _ => fail("did not redirect to the error page")
          }
      }
    }

    "redirect to confirm page when the form is completed successfully" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(vehicleAndKeeperDetailsModel())
        .withCookies(vehicleAndKeeperLookupFormModel())
        .withCookies(captureCertificateDetailsFormModel())
        .withCookies(captureCertificateDetailsModel())
      val (captureCertificateDetails, dateService, auditService) = build()
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
              model.certificateRegistrationMark should equal(RegistrationNumberValid.toUpperCase)
              model.certificateTime should equal(CertificateTimeValid.toUpperCase)
              model.prVrm should equal(RegistrationNumberValid.toUpperCase)
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
      val (captureCertificateDetails, dateService, auditService) = buildWithDirectToPaper()
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
              model.outstandingDates.size should equal(1)
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
      val (captureCertificateDetails, dateService, auditService) = buildWithNotEligible()
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
      val (captureCertificateDetails, dateService, auditService) = build()

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
      val (captureCertificateDetails, dateService, auditService) = build()
      val expected = new AuditRequest(
        name = AuditRequest.CaptureCertificateDetailsToExit,
        serviceType = "PR Assign",
        data = Seq(
          ("transactionId", ClearTextClientSideSessionFactory.DefaultTrackingId),
          ("timestamp", dateService.dateTimeISOChronology),
          ("currentVrm", RegistrationNumberValid),
          ("make", VehicleMakeValid.get),
          ("model", VehicleModelValid.get),
          ("keeperName", "MR DAVID JONES"),
          ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA")
        )
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

      whenReady(result, timeout) { r =>
        verify(auditService, times(1)).send(expected)
      }
    }
  }

  "back" should {
    "redirect to Vehicle Lookup page when the user is a keeper" in new WithApplication {
      whenReady(back(KeeperConsentValid)) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to Confirm Business page when the user is a keeper" in new WithApplication {
      whenReady(back(BusinessConsentValid)) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmBusinessPage.address))
      }
    }
  }

  private def back(keeperConsent: String) = {
    val request = FakeRequest().
      withCookies(
        vehicleAndKeeperLookupFormModel(keeperConsent = keeperConsent),
        vehicleAndKeeperDetailsModel()
      )
    val (captureCertificateDetails, _, _) = build()
    captureCertificateDetails.back(request)
  }

  private def build() = {
    val ioc = testInjector()
    (ioc.getInstance(classOf[CaptureCertificateDetails]), ioc.getInstance(classOf[DateService]), ioc.getInstance(classOf[AuditService]))
  }

  private def buildWithDirectToPaper() = {
    val ioc = testInjector(
      new VrmAssignEligibilityCallDirectToPaperError
    )
    (ioc.getInstance(classOf[CaptureCertificateDetails]), ioc.getInstance(classOf[DateService]), ioc.getInstance(classOf[AuditService]))
  }

  private def buildWithNotEligible() = {
    val ioc = testInjector(
      new VrmAssignEligibilityCallNotEligibleError
    )
    (ioc.getInstance(classOf[CaptureCertificateDetails]), ioc.getInstance(classOf[DateService]), ioc.getInstance(classOf[AuditService]))
  }

  private def buildCorrectlyPopulatedRequest(certificateDate: String = CertificateDateValid,
                                             certificateDocumentCount: String = CertificateDocumentCountValid,
                                             certificateRegistrationMark: String = RegistrationNumberValid,
                                             certificateTime: String = CertificateTimeValid,
                                             prVrm: String = RegistrationNumberValid) = {
    FakeRequest().withFormUrlEncodedBody(
      CertificateDateId -> certificateDate,
      CertificateDocumentCountId -> certificateDocumentCount,
      CertificateRegistrationMarkId -> certificateRegistrationMark,
      CertificateTimeId -> certificateTime,
      PrVrmId -> prVrm)
  }
}