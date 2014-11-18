package mappings.common.vrm_assign

import play.api.data.Mapping
import uk.gov.dvla.vehicles.presentation.common
import common.views.helpers.FormExtensions.nonEmptyTextWithTransform
import play.api.data.validation.Constraint
import play.api.data.validation.Constraints._

object ReferenceNumber {
  final val MinLength = 14
  final val MaxLength = 20

  def referenceNumberMapping: Mapping[String] =
    nonEmptyTextWithTransform(_.toUpperCase.trim)(MinLength, MaxLength) verifying validReferenceNumber

  // TODO move to a constriants package
  def validReferenceNumber: Constraint[String] = pattern(
    regex = """^[a-zA-Z0-9][a-zA-Z0-9\s\-\'\,]*$""".r,
    name = "constraint.validReferenceNumber",
    error = "error.validReferenceNumber")
}
