package controllers

import composition.TestConfig
import controllers.Common.PrototypeHtml
import helpers.UnitSpec
import helpers.WithApplication
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.SERVICE_UNAVAILABLE
import play.api.test.Helpers.status

class MicroserviceErrorUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      status(present) should equal(SERVICE_UNAVAILABLE)
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      val result = microServiceErrorPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  private def microServiceErrorPrototypeNotVisible = {
    testInjector(new TestConfig(isPrototypeBannerVisible = false))
      .getInstance(classOf[MicroServiceError])
  }

  private def present = microServiceError.present(FakeRequest())

  private def microServiceError = testInjector().getInstance(classOf[MicroServiceError])
}
