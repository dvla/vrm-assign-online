package pages

import uk.gov.dvla.vehicles.presentation.common.testhelpers.ApplicationContext
import ApplicationContext.ApplicationRoot

package object vrm_assign {
  final implicit val applicationContext: ApplicationRoot = "/"
  def buildAppUrl(urlPart: String) = ApplicationContext.buildAppUrl(urlPart)
}
