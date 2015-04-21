package mappings.common.vrm_assign

import play.api.data.Mapping
import play.api.data.validation.Constraint
import play.api.data.validation.Constraints._
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.nonEmptyTextWithTransform

object CertificateTime {

  final val MinLength = 6
  final val MaxLength = 6

  def certificateTimeMapping: Mapping[String] =
    nonEmptyTextWithTransform(_.toUpperCase.trim)(MinLength, MaxLength) verifying validCertificateTime

  // TODO move to a constraints package
  def validCertificateTime: Constraint[String] = pattern(
    regex = """^[0-9][0-9\s\-\'\,]*$""".r,
    name = "constraint.validCertificateTime",
    error = "error.validCertificateTime")
}