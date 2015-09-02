package mappings.common.vrm_assign

import play.api.data.Mapping
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.textWithTransform
import views.constraints.Postcode.validPostcode

object Postcode {

  private final val MinLength = 0
  final val MaxLength = 8

  def postcode: Mapping[String] = {
    textWithTransform(_.toUpperCase.trim)(MinLength, MaxLength) verifying validPostcode
  }
}