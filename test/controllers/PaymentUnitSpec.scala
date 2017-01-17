package controllers

import composition.webserviceclients.paymentsolve.TestPaymentWebServiceBinding.{beginWebPaymentUrl, loadBalancerUrl}
import composition.webserviceclients.paymentsolve.{CancelValidated, PaymentCallFails, RefererFromHeaderBinding, ValidatedAuthorised}
import composition.webserviceclients.paymentsolve.{ValidatedCardDetails, ValidatedNotAuthorised, ValidatedNotCardDetails}
import helpers.{TestWithApplication, UnitSpec}
import helpers.vrm_assign.CookieFactoryForUnitSpecs.{captureCertificateDetailsFormModel, captureCertificateDetailsModel}
import helpers.vrm_assign.CookieFactoryForUnitSpecs.{confirmFormModel, granteeConsent, paymentModel, paymentTransNo, transactionId}
import helpers.vrm_assign.CookieFactoryForUnitSpecs.{vehicleAndKeeperDetailsModel, vehicleAndKeeperLookupFormModel}
import pages.vrm_assign.{ConfirmPaymentPage, FulfilPage, LeaveFeedbackPage, PaymentFailurePage, PaymentNotAuthorisedPage}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.test.Helpers.{LOCATION, OK, REFERER, SEE_OTHER, contentAsString, defaultAwaitTimeout}
import webserviceclients.fakes.CaptureCertificateDetailsWebServiceConstants.ExpiredWithFeeCertificate

class PaymentUnitSpec extends UnitSpec {

  "begin" should {
    "redirect to ConfirmPaymentPage when TransactionId cookie does not exist" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          paymentModel(),
          captureCertificateDetailsModel()
        )

      val result = payment.begin(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmPaymentPage.address))
      }
    }

    "redirect to ConfirmPaymentPage when " +
      "CaptureCertificateDetailsFormModel cookie does not exist" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          transactionId(),
          paymentTransNo(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          paymentModel(),
          granteeConsent()
        )

      val result = payment.begin(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmPaymentPage.address))
      }
    }

    "redirect to PaymentFailurePage when no referer in request" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          transactionId(),
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          paymentModel(),
          captureCertificateDetailsFormModel(),
          granteeConsent()
        )

      val result = payment.begin(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to PaymentFailure page when required cookies " +
      "and referer exist and payment service status is not 'CARD_DETAILS'" in new TestWithApplication {
      val payment = testInjector(
        new ValidatedNotCardDetails
      ).getInstance(classOf[Payment])
      val result = payment.begin(requestWithValidDefaults())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to PaymentFailurePage when payment service call throws an exception" in new TestWithApplication {
      val result = paymentCallFails.begin(requestWithValidDefaults())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "display the Payment page when required cookies and referer exist " +
      "and payment service response is 'validated' and status is 'CARD_DETAILS'" in new TestWithApplication {
      val result = payment.begin(requestWithValidDefaults())
      whenReady(result, timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "display the Payment page with an iframe with src url returned by payment micro-service" in new TestWithApplication {
      val result = payment.begin(requestWithValidDefaults())
      val content = contentAsString(result)
      content should include("<iframe")
      content should include( s"""src="$beginWebPaymentUrl"""")
    }
  }

  "getWebPayment" should {
    "redirect to PaymentFailurePage when TransactionId cookie does not exist" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          paymentModel(),
          captureCertificateDetailsModel()
        )
      val result = payment.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to PaymentFailurePage when PaymentTransactionReference cookie does not exist" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          transactionId(),
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          captureCertificateDetailsModel()
        )
      val result = payment.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to PaymentFailurePage when payment service call throws an exception" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          transactionId(),
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          paymentModel(),
          captureCertificateDetailsModel()
        )
      val result = paymentCallFails.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to PaymentNotAuthorised page when payment service status is not 'AUTHORISED'" in new TestWithApplication {
      val payment = testInjector(
        new ValidatedNotAuthorised
      ).getInstance(classOf[Payment])
      val result = payment.getWebPayment(requestWithValidDefaults())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentNotAuthorisedPage.address))
      }
    }

    "redirect to Fulfil page when payment service response is status is 'AUTHORISED'" in new TestWithApplication {
      val payment = testInjector(
        new ValidatedAuthorised
      ).getInstance(classOf[Payment])
      val request = FakeRequest()
        .withCookies(
          transactionId(),
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          paymentModel(),
          captureCertificateDetailsModel()
        )
      val result = payment.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(FulfilPage.address))
      }
    }
  }

  "cancel" should {
    "redirect to LeaveFeedbackPage when TransactionId cookie does not exist" in new TestWithApplication {
      val result = paymentCancelValidated.cancel(requestWithValidDefaults())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(LeaveFeedbackPage.address))
      }
    }

    "redirect to PaymentFailurePage when paymentModel cookie does not exist" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          transactionId(),
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          captureCertificateDetailsModel()
        )
      val result = paymentCancelValidated.cancel(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to LeaveFeedback page when required cookies exist" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          transactionId(),
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          paymentModel(),
          captureCertificateDetailsModel(),
          captureCertificateDetailsFormModel()
        )
      val cancel = paymentCancelValidated.cancel(request)

      whenReady(cancel) { r =>
        r.header.headers.get(LOCATION) should equal(Some(LeaveFeedbackPage.address))
      }
    }
  }

  "callback" should {
    "should redirect" in new TestWithApplication {
      val result = payment.callback("stub token")(FakeRequest())

      whenReady(result) { r =>
        r.header.status should equal(SEE_OTHER)
        r.header.headers.get(LOCATION) should equal(Some(controllers.routes.Payment.getWebPayment().url))
      }
    }
  }

  private def requestWithValidDefaults(referer: String = loadBalancerUrl): FakeRequest[AnyContentAsEmpty.type] = {
    val refererHeader = (REFERER, Seq(referer))
    val headers = FakeHeaders(data = Seq(refererHeader))
    FakeRequest(method = "GET", uri = "/", headers = headers, body = AnyContentAsEmpty)
      .withCookies(
        transactionId(),
        paymentTransNo(),
        vehicleAndKeeperLookupFormModel(registrationNumber = "DD22"),
        vehicleAndKeeperDetailsModel(registrationNumber = "DD22"),
        confirmFormModel(),
        paymentModel(),
        captureCertificateDetailsModel(certificate = ExpiredWithFeeCertificate),
        captureCertificateDetailsFormModel(),
        granteeConsent()
      )
  }

  private def payment = testInjector(
    new ValidatedCardDetails(),
    new RefererFromHeaderBinding
  ).getInstance(classOf[Payment])

  private def paymentCallFails = testInjector(
    new PaymentCallFails,
    new RefererFromHeaderBinding
  ).getInstance(classOf[Payment])

  private def paymentCancelValidated = testInjector(
    new CancelValidated,
    new RefererFromHeaderBinding
  ).getInstance(classOf[Payment])
}
