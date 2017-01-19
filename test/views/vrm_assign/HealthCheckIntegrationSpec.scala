package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.impl.client.HttpClients
import pages.vrm_assign.HealthCheckPage
import play.mvc.Http.Status
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory

final class HealthCheckIntegrationSpec extends UiSpec with TestHarness {

  "Accessing the /healthcheck url" should {

    "return 200 for GET" in new WebBrowserForSelenium {
      val httpResponse = execute(new HttpGet(HealthCheckPage.url))
      try httpResponse.getStatusLine.getStatusCode should be(Status.OK)
      finally httpResponse.close()
    }

    "return 404 for PUT etc." in new WebBrowserForSelenium {
      val httpResponse = execute(new HttpPut(HealthCheckPage.url))
      try httpResponse.getStatusLine.getStatusCode should be(Status.NOT_FOUND)
      finally httpResponse.close()
    }
  }

  private def execute(method: HttpRequestBase): CloseableHttpResponse = {
    val httpClient = HttpClients.createDefault()
    httpClient.execute(method)
  }
}
