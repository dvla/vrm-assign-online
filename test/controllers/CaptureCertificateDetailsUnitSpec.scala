package controllers

import composition.webserviceclients.vrmassigneligibility.VrmAssignEligibilityCallDirectToPaperError
import composition.webserviceclients.vrmassigneligibility.VrmAssignEligibilityCallNotEligibleError
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.JsonUtils.deserializeJsonToModel
import helpers.UnitSpec
import helpers.vrm_assign.CookieFactoryForUnitSpecs.captureCertificateDetailsFormModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.captureCertificateDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.trackingIdModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import helpers.WithApplication
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import org.joda.time.format.DateTimeFormat
import org.mockito.Mockito.{times, verify}
import pages.vrm_assign.ConfirmBusinessPage
import pages.vrm_assign.ConfirmPage
import pages.vrm_assign.LeaveFeedbackPage
import pages.vrm_assign.VehicleLookupFailurePage
import pages.vrm_assign.VehicleLookupPage
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{TrackingId, ClearTextClientSideSessionFactory}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
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
    "display the page" in new WithApplication {
      val request = FakeRequest()
        .withCookies(
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
            case Some(url) => url should include(routes.Error.present("user went to CaptureCertificateDetails " +
              "submit without the VehicleAndKeeperDetailsModel cookie").url)
            case _ => fail("did not redirect to the error page")
          }
      }
    }

    "redirect to confirm page when the form is completed successfully" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(vehicleAndKeeperDetailsModel())
        .withCookies(vehicleAndKeeperLookupFormModel(replacementVRN = RegistrationNumberValid))
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
            case None => fail(s"$cookieName cookie not found")
          }
      }
    }

    "redirect to vehicles failure page when the form is completed successfully " +
      "but fails eligibility with a direct to paper code" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(vehicleAndKeeperDetailsModel())
        .withCookies(vehicleAndKeeperLookupFormModel(replacementVRN = RegistrationNumberValid))
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
              model.outstandingDates.size should equal(2)
            case None => fail(s"$cookieName cookie not found")
          }
      }
    }

    "redirect to vehicles failure page when the form is completed successfully " +
      "but fails eligibility with a not eligible code" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(vehicleAndKeeperDetailsModel())
        .withCookies(vehicleAndKeeperLookupFormModel(replacementVRN = RegistrationNumberValid))
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
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperDetailsModel()
        )
      val (captureCertificateDetails, dateService, auditService) = build()
      val result = captureCertificateDetails.exit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(LeaveFeedbackPage.address))
      }
    }

    "call audit service once with 'default_test_tracking_id' when the required cookies exist" in new WithApplication {
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

      whenReady(result, timeout) { r =>
        verify(auditService, times(1)).send(expected, TrackingId("trackingId"))
      }
    }

    "call audit service once with expected values when the required cookies exist" in new WithApplication {
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

      whenReady(result, timeout) { r =>
        verify(auditService, times(1)).send(expected, TrackingId("trackingId"))
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

  "calculateYearsOwed" should {
    "return no charge for an expiry date after the abolition date" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(vehicleAndKeeperDetailsModel())
        .withCookies(vehicleAndKeeperLookupFormModel(replacementVRN = RegistrationNumberValid))
        .withCookies(captureCertificateDetailsFormModel())
        .withCookies(captureCertificateDetailsModel())
      val (captureCertificateDetails, dateService, auditService) = build()

      val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
      val expiryDate = formatter.parseDateTime("10/03/2015")
      captureCertificateDetails.calculateYearsOwed(expiryDate).size should equal(0)
    }

    "return no charge for an expiry date on the abolition date" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(vehicleAndKeeperDetailsModel())
        .withCookies(vehicleAndKeeperLookupFormModel(replacementVRN = RegistrationNumberValid))
        .withCookies(captureCertificateDetailsFormModel())
        .withCookies(captureCertificateDetailsModel())
      val (captureCertificateDetails, dateService, auditService) = build()

      val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
      val expiryDate = formatter.parseDateTime("09/03/2015")
      captureCertificateDetails.calculateYearsOwed(expiryDate).size should equal(0)
    }

    "return 1 charge for an expiry date on the day before abolition date" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(vehicleAndKeeperDetailsModel())
        .withCookies(vehicleAndKeeperLookupFormModel(replacementVRN = RegistrationNumberValid))
        .withCookies(captureCertificateDetailsFormModel())
        .withCookies(captureCertificateDetailsModel())
      val (captureCertificateDetails, dateService, auditService) = build()

      val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
      val expiryDate = formatter.parseDateTime("08/03/2015")
      captureCertificateDetails.calculateYearsOwed(expiryDate).size should equal(1)
    }

    "return 4 charges for an expiry date of 08/03/2012" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(vehicleAndKeeperDetailsModel())
        .withCookies(vehicleAndKeeperLookupFormModel(replacementVRN = RegistrationNumberValid))
        .withCookies(captureCertificateDetailsFormModel())
        .withCookies(captureCertificateDetailsModel())
      val (captureCertificateDetails, dateService, auditService) = build()

      val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
      val expiryDate = formatter.parseDateTime("08/03/2012")
      captureCertificateDetails.calculateYearsOwed(expiryDate).size should equal(4)
    }

    "return 3 charges for an expiry date of 11/11/2012" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(vehicleAndKeeperDetailsModel())
        .withCookies(vehicleAndKeeperLookupFormModel(replacementVRN = RegistrationNumberValid))
        .withCookies(captureCertificateDetailsFormModel())
        .withCookies(captureCertificateDetailsModel())
      val (captureCertificateDetails, dateService, auditService) = build()

      val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
      val expiryDate = formatter.parseDateTime("11/11/2012")
      val result = captureCertificateDetails.calculateYearsOwed(expiryDate)
      result.size should equal(3)
      result.head should include ("11/11/2013")
      result.tail.head should include ("11/11/2014")
      result.tail.tail.head should include ("11/11/2015")
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
    (ioc.getInstance(classOf[CaptureCertificateDetails]),
      ioc.getInstance(classOf[DateService]),
      ioc.getInstance(classOf[AuditService])
      )
  }

  private def buildWithDirectToPaper() = {
    val ioc = testInjector(
      new VrmAssignEligibilityCallDirectToPaperError
    )
    (ioc.getInstance(classOf[CaptureCertificateDetails]),
      ioc.getInstance(classOf[DateService]),
      ioc.getInstance(classOf[AuditService])
      )
  }

  private def buildWithNotEligible() = {
    val ioc = testInjector(
      new VrmAssignEligibilityCallNotEligibleError
    )
    (ioc.getInstance(classOf[CaptureCertificateDetails]),
      ioc.getInstance(classOf[DateService]),
      ioc.getInstance(classOf[AuditService])
      )
  }

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