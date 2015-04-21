package mappings.common.vrm_assign

import play.api.data.Mapping
import play.api.data.validation.Constraint
import play.api.data.validation.Constraints._
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.nonEmptyTextWithTransform

object CertificateDate {
  final val MinLength = 5
  final val MaxLength = 5

  def certificateDateMapping: Mapping[String] =
    nonEmptyTextWithTransform(_.toUpperCase.trim)(MinLength, MaxLength) verifying validCertificateDate

  // TODO move to a constraints package
  def validCertificateDate: Constraint[String] = pattern(
    regex = """^[0-9][0-9\s\-\'\,]*$""".r,
    name = "constraint.validCertificateDate",
    error = "error.validCertificateDate")
}