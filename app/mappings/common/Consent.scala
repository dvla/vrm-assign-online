package mappings.common

import play.api.data.Forms.nonEmptyText
import play.api.data.Mapping

object Consent {
  def consent: Mapping[String] = nonEmptyText
}