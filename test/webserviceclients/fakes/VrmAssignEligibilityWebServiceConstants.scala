package webserviceclients.fakes

import org.joda.time.DateTime
import play.api.http.Status._
import webserviceclients.vrmretentioneligibility.VrmAssignEligibilityResponse

object VrmAssignEligibilityWebServiceConstants {

  def vrmAssignEligibilityResponseSuccess: (Int, Option[VrmAssignEligibilityResponse]) = {
    (OK, Some(VrmAssignEligibilityResponse(certificateExpiryDate = Some(new DateTime), responseCode = None)))
  }

}