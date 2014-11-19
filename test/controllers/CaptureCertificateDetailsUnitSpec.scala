package controllers

import composition.TestAuditService
import helpers.vrm_assign.CookieFactoryForUnitSpecs._
import helpers.{UnitSpec, WithApplication}
import play.api.http.Status.OK
import play.api.test.FakeRequest

final class CaptureCertificateDetailsUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperDetailsModel()
        )
      val result = checkEligibility().present(request)

      result.futureValue.header.status should equal(OK)
    }
  }

  private def checkEligibility() = {
    testInjector(
      new TestAuditService()
    ).
      getInstance(classOf[CaptureCertificateDetails])
  }
}