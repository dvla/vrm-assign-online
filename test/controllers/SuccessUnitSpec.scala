package controllers

import composition.TestAssignEmailServiceBinding
import composition.webserviceclients.paymentsolve.ValidatedAuthorised
import helpers.UnitSpec
import helpers.vrm_assign.CookieFactoryForUnitSpecs.businessDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.captureCertificateDetailsFormModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.captureCertificateDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.confirmFormModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.fulfilModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.paymentModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.paymentTransNo
import helpers.vrm_assign.CookieFactoryForUnitSpecs.setupBusinessDetails
import helpers.vrm_assign.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import helpers.WithApplication
import models.BusinessDetailsModel
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.ConfirmFormModel
import models.VehicleAndKeeperLookupFormModel
import org.mockito.{Mockito, Matchers}
import org.mockito.Matchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.scalatest.mock.MockitoSugar
import pages.vrm_assign.SuccessPage
import play.api.test.FakeRequest
import play.api.test.Helpers.BAD_REQUEST
import play.api.test.Helpers.CONTENT_DISPOSITION
import play.api.test.Helpers.CONTENT_TYPE
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.OK
import play.api.test.Helpers.status
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import webserviceclients.fakes.AddressLookupServiceConstants.KeeperEmailValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.BusinessConsentValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.ReplacementVRN

class SuccessUnitSpec extends UnitSpec with MockitoSugar {

  "present" should {
    "display the page when BusinessDetailsModel cookie exists" in new WithApplication {
      val request = FakeRequest()
        .withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())

      val (success, _) = build
      val result = success.present(request)
      status(result) should equal(OK)
    }

    "display the page when BusinessDetailsModel cookie does not exists" in new WithApplication {
      val request = FakeRequest()
        .withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          confirmFormModel(),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())

      val (success, _) = build
      val result = success.present(request)
      status(result) should equal(OK)
    }

    "call the email service when businessDetails cookie exists" in new WithApplication {
      val isKeeper = false
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
          setupBusinessDetails(),
          businessDetailsModel(),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          confirmFormModel(keeperEmail = None),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (success, emailService) = build
      val result = success.present(request)
      whenReady(result) { r =>
        // verify no email was sent in present
        Mockito.verifyNoMoreInteractions(emailService)
      }
    }

    "call the email service when keeper selected to supply an " +
      "email address and did supply an email" in new WithApplication {
      val isKeeper = true
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          confirmFormModel(keeperEmail = KeeperEmailValid),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (success, emailService) = build
      val result = success.present(request)
      whenReady(result) { r =>
        // verify no email was sent in present
        Mockito.verifyNoMoreInteractions(emailService)
      }
    }

    "not call the email service when businessDetails does not cookie" in new WithApplication {
      val isKeeper = false
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          confirmFormModel(keeperEmail = None),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (success, emailService) = build
      val result = success.present(request)
      whenReady(result) { r =>
        // verify no email was sent in present
        Mockito.verifyNoMoreInteractions(emailService)
      }
    }

    "not call the email service when keeper did not select to supply an email address" in new WithApplication {
      val isKeeper = true
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          confirmFormModel(keeperEmail = None),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (success, emailService) = build
      val result = success.present(request)
      whenReady(result) { r =>
        verify(emailService, never).emailRequest(
          any[String],
          any[VehicleAndKeeperDetailsModel],
          any[CaptureCertificateDetailsFormModel],
          any[CaptureCertificateDetailsModel],
          any[VehicleAndKeeperLookupFormModel],
          any[String],
          any[String],
          any[Option[ConfirmFormModel]],
          any[Option[BusinessDetailsModel]],
          Matchers.eq(isKeeper),
          any[TrackingId]
        )
        Mockito.verifyNoMoreInteractions(emailService)
      }
    }
  }

  "create pdf" should {
    "return a bad request if cookie for EligibilityModel does no exist" in new WithApplication {
      val request = FakeRequest().withCookies(transactionId())
      val (success, _) = build
      val result = success.createPdf(request)
      status(result) should equal(BAD_REQUEST)
    }

    "return a bad request if cookie for TransactionId does no exist" in new WithApplication {
      val request = FakeRequest().withCookies(fulfilModel())
      val (success, _) = build
      val result = success.createPdf(request)
      status(result) should equal(BAD_REQUEST)
    }

    "return a pdf when the cookie exists" in new WithApplication {
      val request = FakeRequest()
        .withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (success, _) = build
      val result = success.createPdf(request)
      whenReady(result) { r =>
        r.header.status should equal(OK)
        r.header.headers.get(CONTENT_DISPOSITION) should equal(Some(s"attachment;filename=${ReplacementVRN}-eV948.pdf"))
        r.header.headers.get(CONTENT_TYPE) should equal(Some("application/pdf"))
      }
    }
  }

  private def build = {
    val assignEmailService = new TestAssignEmailServiceBinding
    val injector = testInjector(
      new ValidatedAuthorised(),
      assignEmailService
    )
    (injector.getInstance(classOf[Success]), assignEmailService.stub)
  }
}
