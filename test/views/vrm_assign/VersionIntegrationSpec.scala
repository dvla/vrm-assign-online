package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import org.scalatest.selenium.WebBrowser.{go, pageSource}
import pages.vrm_assign.VersionPage
import scala.io.Source.fromInputStream

class VersionIntegrationSpec extends UiSpec with TestHarness {

  "Version endpoint" should {
    "be declared and should include the build-details.txt from classpath" in new WebBrowserForSelenium {
      go to VersionPage
      val t = fromInputStream(getClass.getResourceAsStream("/build-details.txt")).getLines().toSet.toList
      pageSource.lines.toSet.toList should contain allOf(t.head, t.tail.head, t.tail.tail.toSeq: _*)
    }
  }
}
