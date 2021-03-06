package controllers

import composition.TestWithDefaultApplication
import helpers.UnitSpec
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.REFERER
import webserviceclients.paymentsolve.RefererFromHeaderImpl

final class RefererFromHeaderImplSpec extends UnitSpec {

  "fetch" should {
    "return none when request does not contain a referer" in new TestWithDefaultApplication {
      val refererFromHeader = new RefererFromHeaderImpl
      val request: Request[_] = FakeRequest()
      refererFromHeader.fetch(request) should equal(None)
    }

    "return expected value when request contains referer with hyphens" in new TestWithDefaultApplication {
      val referer = s"$loadBalancer${routes.Confirm.present()}"
      checkExpectedReferer(referer)
    }

    "return expected value when request contains referer with hyphens and full stops" in new TestWithDefaultApplication {
      val referer = s"$loadBalancer.co.uk${routes.Confirm.present()}"
      checkExpectedReferer(referer)
    }

    "return expected value when request contains referer with hyphens, " +
      "full stops and port number" in new TestWithDefaultApplication {
      val referer = s"$loadBalancer.co.uk:443${routes.Confirm.present()}"
      checkExpectedReferer(referer)
    }
  }

  "paymentCallbackUrl" should {
    "return expected value when request contains referer with hyphens" in new TestWithDefaultApplication {
      val referer = s"$loadBalancer${routes.Confirm.present()}"
      val tokenBase64URLSafe = "01234"
      val refererFromHeader = new RefererFromHeaderImpl
      refererFromHeader.paymentCallbackUrl(referer = referer,
        tokenBase64URLSafe = tokenBase64URLSafe) should equal(s"$loadBalancer/payment/callback/01234")
    }

    "return expected value when request contains referer with hyphens and full stops" in new TestWithDefaultApplication {
      val referer = s"$loadBalancer.co.uk${routes.Confirm.present()}"
      val tokenBase64URLSafe = "01234"
      val refererFromHeader = new RefererFromHeaderImpl
      refererFromHeader.paymentCallbackUrl(referer = referer,
        tokenBase64URLSafe = tokenBase64URLSafe) should equal(s"$loadBalancer.co.uk/payment/callback/01234")
    }

    "return expected value when request contains referer with hyphens, " +
      "full stops and port number" in new TestWithDefaultApplication {
      val referer = s"$loadBalancer.co.uk:443${routes.Confirm.present()}.co.uk"
      val tokenBase64URLSafe = "01234"
      val refererFromHeader = new RefererFromHeaderImpl
      refererFromHeader.paymentCallbackUrl(referer = referer,
        tokenBase64URLSafe = tokenBase64URLSafe) should equal(s"$loadBalancer.co.uk:443/payment/callback/01234")
    }
  }

  private val loadBalancer = "https://somewhere-in-load-balancer-land"

  private def checkExpectedReferer(expected: String) = {
    val refererFromHeader = new RefererFromHeaderImpl
    val request: Request[_] = FakeRequest().withHeaders(REFERER -> expected)
    refererFromHeader.fetch(request) should equal(Some(expected))
  }
}
