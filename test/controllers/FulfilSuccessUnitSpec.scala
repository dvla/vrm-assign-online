package controllers

import composition.TestAssignEmailServiceBinding
import composition.TestEmailService
import composition.WithApplication
import composition.webserviceclients.paymentsolve.ValidatedAuthorised
import helpers.UnitSpec
import helpers.vrm_assign.CookieFactoryForUnitSpecs._
import models.BusinessDetailsModel
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.ConfirmFormModel
import models.FulfilModel
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.scalatest.mock.MockitoSugar
import pages.vrm_assign.SuccessPage
import play.api.test.FakeRequest
import play.api.test.Helpers.BAD_REQUEST
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.status
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import views.vrm_assign.Confirm.SupplyEmail_false
import webserviceclients.emailservice.EmailServiceSendRequest
import webserviceclients.fakes.AddressLookupServiceConstants.KeeperEmailValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.BusinessConsentValid

final class FulfilSuccessUnitSpec extends UnitSpec with MockitoSugar {

  "present" should {
    "display the page when BusinessDetailsModel cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          businessChooseYourAddress(),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (successPayment, _) = build
      val result = successPayment.present(request)
      println(contentAsString(result))

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))
      }
    }

    "display the page when BusinessDetailsModel cookie does not exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          businessChooseYourAddress(),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          confirmFormModel(),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (successPayment, _) = build
      val result = successPayment.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))
      }
    }

    "call the email service when businessDetails cookie exists" in new WithApplication {
      val isKeeper = false
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
          setupBusinessDetails(),
          businessChooseYourAddress(),
          businessDetailsModel(),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          confirmFormModel(keeperEmail = None, supplyEmail = SupplyEmail_false),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (successPayment, emailService) = build
      val result = successPayment.present(request)
      whenReady(result) { r =>
        verify(emailService, times(1)).sendEmail(
          any[String],
          any[VehicleAndKeeperDetailsModel],
          any[CaptureCertificateDetailsFormModel],
          any[CaptureCertificateDetailsModel],
          any[FulfilModel],
          any[String],
          any[Option[ConfirmFormModel]],
          any[Option[BusinessDetailsModel]],
          Matchers.eq(isKeeper),
          any[String]
        )
      }
    }

    "call the email service when keeper selected to supply an email address and did supply an email" in new WithApplication {
      val isKeeper = true
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          confirmFormModel(keeperEmail = KeeperEmailValid, supplyEmail = supplyEmailTrue),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (successPayment, emailService) = build
      val result = successPayment.present(request)
      whenReady(result) { r =>
        verify(emailService, times(1)).sendEmail(
          any[String],
          any[VehicleAndKeeperDetailsModel],
          any[CaptureCertificateDetailsFormModel],
          any[CaptureCertificateDetailsModel],
          any[FulfilModel],
          any[String],
          any[Option[ConfirmFormModel]],
          any[Option[BusinessDetailsModel]],
          Matchers.eq(isKeeper),
          any[String]
        )
      }
    }

    "not call the email service when businessDetails does not cookie" in new WithApplication {
      val isKeeper = false
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          confirmFormModel(keeperEmail = None, supplyEmail = supplyEmailTrue),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (successPayment, emailService) = build
      val result = successPayment.present(request)
      whenReady(result) { r =>
        verify(emailService, never).sendEmail(
          any[String],
          any[VehicleAndKeeperDetailsModel],
          any[CaptureCertificateDetailsFormModel],
          any[CaptureCertificateDetailsModel],
          any[FulfilModel],
          any[String],
          any[Option[ConfirmFormModel]],
          any[Option[BusinessDetailsModel]],
          Matchers.eq(isKeeper),
          any[String]
        )
      }
    }

    "not call the email service when keeper did not select to supply an email address" in new WithApplication {
      val isKeeper = true
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          confirmFormModel(keeperEmail = None, supplyEmail = "no"),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (successPayment, emailService) = build
      val result = successPayment.present(request)
      whenReady(result) { r =>
        verify(emailService, never).sendEmail(
          any[String],
          any[VehicleAndKeeperDetailsModel],
          any[CaptureCertificateDetailsFormModel],
          any[CaptureCertificateDetailsModel],
          any[FulfilModel],
          any[String],
          any[Option[ConfirmFormModel]],
          any[Option[BusinessDetailsModel]],
          Matchers.eq(isKeeper),
          any[String]
        )
      }
    }

    "not call the email service when keeper did not select to supply an email address but did provide one" in new WithApplication {
      val isKeeper = true
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          confirmFormModel(keeperEmail = KeeperEmailValid, supplyEmail = "no"),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (successPayment, emailService) = build
      val result = successPayment.present(request)
      whenReady(result) { r =>
        verify(emailService, never).sendEmail(
          any[String],
          any[VehicleAndKeeperDetailsModel],
          any[CaptureCertificateDetailsFormModel],
          any[CaptureCertificateDetailsModel],
          any[FulfilModel],
          any[String],
          any[Option[ConfirmFormModel]],
          any[Option[BusinessDetailsModel]],
          Matchers.eq(isKeeper),
          any[String]
        )
      }
    }
  }

  "create pdf" should {
    "return a bad request if cookie for EligibilityModel does no exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(transactionId())
      val (successPayment, _) = build
      val result = successPayment.createPdf(request)
      status(result) should equal(BAD_REQUEST)
    }

    "return a bad request if cookie for TransactionId does no exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(fulfilModel())
      val (successPayment, _) = build
      val result = successPayment.createPdf(request)
      status(result) should equal(BAD_REQUEST)
    }

    "return a pdf when the cookie exists" in pending
  }

  private def build = {
    val assignEmailService = new TestAssignEmailServiceBinding
    val injector = testInjector(
      new ValidatedAuthorised(),
      assignEmailService
    )
    (injector.getInstance(classOf[FulfilSuccess]), assignEmailService.stub)
  }

  private val supplyEmailTrue = "true"
}
