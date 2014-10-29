package helpers.steps.hooks

import cucumber.api.java.After
import helpers.webbrowser.WebBrowserDriver
import org.openqa.selenium.WebDriver

final class WebBrowserHooks(webBrowserDriver: WebBrowserDriver) {

  @After
  def quitBrowser() = {
    implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]
    webDriver.quit()
  }
}