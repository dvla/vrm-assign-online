package mappings.common.vrm_assign

import play.api.data.Mapping
import play.api.data.validation.Constraint
import play.api.data.validation.Constraints._
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.nonEmptyTextWithTransform

object CertificateRegistrationMark {
  final val MinLength = 2
  final val MaxLength = 8

  def certificateRegistrationMarkMapping: Mapping[String] =
    nonEmptyTextWithTransform(_.toUpperCase.trim)(MinLength, MaxLength) verifying validCertificateRegistrationMark

  // TODO move to a constraints package
  def validCertificateRegistrationMark: Constraint[String] = pattern(
    regex = """^[a-zA-Z0-9][a-zA-Z0-9\s\-\'\,]*$""".r,
    name = "constraint.validCertificateRegistrationMark",
    error = "error.validCertificateRegistrationMark")
}