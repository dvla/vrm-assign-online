package pages.common

import org.openqa.selenium.WebDriver
import uk.gov.dvla.vehicles.presentation.common.controllers.AlternateLanguages.{CyId, EnId}
import org.scalatest.selenium.WebBrowser.find
import org.scalatest.selenium.WebBrowser.id
import org.scalatest.selenium.WebBrowser.Element

object AlternateLanguages {
  def cymraeg(implicit driver: WebDriver): Element = find(id(CyId)).get
  def english(implicit driver: WebDriver): Element = find(id(EnId)).get
  def isCymraegDisplayed(implicit driver: WebDriver): Boolean = find(id(CyId)).isDefined
  def isEnglishDisplayed(implicit driver: WebDriver): Boolean = find(id(EnId)).isDefined
}