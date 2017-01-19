package pages.vrm_assign

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}

object LeaveFeedbackPage extends Page {

  def address = buildAppUrl("leave-feedback")

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = ""
  final val titleCy: String = ""
}
