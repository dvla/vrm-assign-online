package helpers.webbrowser;

import org.openqa.selenium.support.events.EventFiringWebDriver;

final public class WebBrowserDriverWIthJavaScript extends EventFiringWebDriver {

    public WebBrowserDriverWIthJavaScript() {
        super(WebDriverFactory.webDriver( "htmlunit", true));
    }
}