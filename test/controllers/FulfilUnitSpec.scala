package controllers

import composition.RefererFromHeaderBinding
import composition.WithApplication
import composition.webserviceclients.paymentsolve.TestPaymentWebServiceBinding.loadBalancerUrl
import composition.webserviceclients.paymentsolve.ValidatedCardDetails
import controllers.Payment.AuthorisedStatus
import helpers.UnitSpec
import helpers.vrm_assign.CookieFactoryForUnitSpecs.captureCertificateDetailsFormModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.captureCertificateDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.confirmFormModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.granteeConsent
import helpers.vrm_assign.CookieFactoryForUnitSpecs.paymentModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.paymentTransNo
import helpers.vrm_assign.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeHeaders
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, REFERER}

class FulfilUnitSpec extends UnitSpec {

  "fulfil" should {
    "redirect to ErrorPage when cookies do not exist" in new WithApplication {
      val request = FakeRequest()

      val result = fulfil.fulfil(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some("/error/user%20went%20to%20fulfil%20mark%20without%20correct%20cookies"))
      }
    }

    "redirect to FulfilSuccessPage when no fees due and required cookies are present" in new WithApplication {
      val result = fulfil.fulfil(requestWithFeesNotDue())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some("/fulfil-success"))
      }
    }

    "redirect to FulfilSuccessPage when fees due and required cookies are present" in new WithApplication {
      val result = fulfil.fulfil(requestWithFeesDue())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some("/fulfil-success"))
      }
    }

    "redirect to ErrorPage when there are fees due but the payment status is not AUTHORISED" in new WithApplication {
      val result = fulfil.fulfil(requestWithFeesDue(paymentStatus = None))
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some("/error/user%20went%20to%20fulfil%20mark%20without%20correct%20cookies"))
      }
    }
  }

  private def requestWithFeesNotDue(referer: String = loadBalancerUrl): FakeRequest[AnyContentAsEmpty.type] = {
    val refererHeader = (REFERER, Seq(referer))
    val headers = FakeHeaders(data = Seq(refererHeader))
    FakeRequest(method = "GET", uri = "/", headers = headers, body = AnyContentAsEmpty).
      withCookies(
        vehicleAndKeeperLookupFormModel(),
        transactionId(),
        captureCertificateDetailsFormModel(),
        granteeConsent(),
        captureCertificateDetailsModel(),
        vehicleAndKeeperDetailsModel(),
        confirmFormModel()
      )
  }

  private def requestWithFeesDue(referer: String = loadBalancerUrl, paymentStatus: Option[String] = Some(AuthorisedStatus)): FakeRequest[AnyContentAsEmpty.type] = {
    val refererHeader = (REFERER, Seq(referer))
    val headers = FakeHeaders(data = Seq(refererHeader))
    FakeRequest(method = "GET", uri = "/", headers = headers, body = AnyContentAsEmpty).
      withCookies(
        vehicleAndKeeperLookupFormModel(registrationNumber = "DD22"),
        transactionId(),
        captureCertificateDetailsFormModel(),
        granteeConsent(),
        captureCertificateDetailsModel(fees = 42),
        paymentModel(paymentStatus = paymentStatus),
        paymentTransNo(),
        vehicleAndKeeperDetailsModel(registrationNumber = "DD22"),
        confirmFormModel()
      )
  }

  private def fulfil = testInjector(
    new ValidatedCardDetails(),
    new RefererFromHeaderBinding
  ).getInstance(classOf[Fulfil])
}