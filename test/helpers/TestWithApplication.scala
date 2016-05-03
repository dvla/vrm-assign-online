package helpers

import composition.TestGlobal
import helpers.TestWithApplication.fakeAppWithTestGlobal
import play.api.test.FakeApplication
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication

abstract class TestWithApplication(app: FakeApplication = fakeAppWithTestGlobal)
  extends play.api.test.WithApplication(app = app)

object TestWithApplication {
  private lazy val fakeAppWithTestGlobal: FakeApplication = LightFakeApplication(TestGlobal)
}
