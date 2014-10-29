package helpers.webbrowser;

import org.openqa.selenium.support.events.EventFiringWebDriver;

final public class WebBrowserDriver extends EventFiringWebDriver {

    public WebBrowserDriver() {
        super(WebDriverFactory.webDriver());
    }
}