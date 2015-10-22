package pages.vrm_assign

import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}

object LeaveFeedbackPage extends Page {

  def address = s"$applicationContext/leave-feedback"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = ""
  final val titleCy: String = ""
}
