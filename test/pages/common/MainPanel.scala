package pages.common

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import views.vrm_assign.Main.BackId

object MainPanel {

  /** back button is removed from the panel. This is left here is case of refactoring */
  def back(implicit driver: WebDriver) = find(id(BackId)).get
}