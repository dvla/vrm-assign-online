package helpers.steps.hooks

import composition.TestGlobal
import cucumber.api.java.After
import cucumber.api.java.Before
import play.api.test.FakeApplication
import play.api.test.TestServer
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication

final class TestServerHooks {

  import helpers.steps.hooks.TestServerHooks.{fakeAppWithTestGlobal, port}

  private val testServer: TestServer = TestServer(port = port, application = fakeAppWithTestGlobal)

  @Before(order = 500)
  def startServer() = {
    testServer.start()
  }

  @After(order = 500)
  def stopServer() = {
    testServer.stop()
  }
}

object TestServerHooks {

  private final val port: Int = 9005
  private lazy val fakeAppWithTestGlobal: FakeApplication = LightFakeApplication(global = TestGlobal)
}