package mappings.common.vrm_assign

import play.api.data.Mapping
import play.api.data.validation.Constraint
import play.api.data.validation.Constraints.pattern
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.nonEmptyTextWithTransform

object CertificateTime {

  final val MinLength = 1
  final val MaxLength = 6

  /**
   * adds padding 0 zeros to the certificate time and then uppercase it.
   * @return
   */
  def certificateTimeMapping: Mapping[String] =
    nonEmptyTextWithTransform( v => if (v.trim.length == 0) {
      v.toUpperCase.trim
    } else {
      f"$v%6s".replace(" ", "0") .toUpperCase.trim
    })(MinLength, MaxLength) verifying validCertificateTime

  def validCertificateTime: Constraint[String] = pattern(
    regex = """^[0-9]*$""".r,
    name = "constraint.validCertificateTime",
    error = "error.validCertificateTime")
}