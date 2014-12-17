package controllers

import audit.{AuditMessage, AuditService}
import composition.{TestAuditService, TestBruteForcePreventionWebService, TestDateService}
import helpers.vrm_assign.CookieFactoryForUnitSpecs._
import helpers.{UnitSpec, WithApplication}
import org.mockito.Mockito._
import pages.vrm_assign.LeaveFeedbackPage
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._

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
    val auditService = mock[AuditService]
    val ioc = testInjector(
      new TestBruteForcePreventionWebService(permitted = true),
      new TestDateService(),
      new TestAuditService(auditService)
    )
    (ioc.getInstance(classOf[CaptureCertificateDetails]), ioc.getInstance(classOf[DateService]), ioc.getInstance(classOf[AuditService]))
  }
}