package pages.vrm_assign

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}

object FulfilPage extends Page {

  def address = buildAppUrl("fulfil")

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Shouldn't call this as page is a controller, nothing is visible"
}
