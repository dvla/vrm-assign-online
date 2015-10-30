package mappings.common.vrm_assign

import play.api.data.Mapping
import play.api.data.validation.Constraint
import play.api.data.validation.Constraints.pattern
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.nonEmptyTextWithTransform

object CertificateDocumentCount {

  final val MinLength = 1
  final val MaxLength = 1

  def certificateDocumentCountMapping: Mapping[String] =
    nonEmptyTextWithTransform(_.toUpperCase.trim)(MinLength, MaxLength) verifying validCertificateDocument

  def validCertificateDocument: Constraint[String] = pattern(
    regex = """^[A-Z0-9]$""".r,
    name = "constraint.validCertificateDocument",
    error = "error.validCertificateDocument")
}
