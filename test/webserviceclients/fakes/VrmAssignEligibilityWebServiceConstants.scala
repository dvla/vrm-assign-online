package webserviceclients.fakes

import org.joda.time.DateTime
import org.joda.time.Period
import play.api.http.Status.OK
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityResponse

object VrmAssignEligibilityWebServiceConstants {

  def vrmAssignEligibilityResponseSuccess: (Int, Option[VrmAssignEligibilityResponse]) = {
    (OK, Some(VrmAssignEligibilityResponse(certificateExpiryDate = Some(new DateTime), responseCode = None)))
  }

  def vrmAssignEligibilityResponseDirectToPaperError: (Int, Option[VrmAssignEligibilityResponse]) = {
    (OK,
      Some(VrmAssignEligibilityResponse(certificateExpiryDate = Some(new DateTime().minus(Period.years(2))),
           responseCode = Some("A5096 - vrm_assign_eligibility_direct_to_paper"))
      )
    )
  }

  def vrmAssignEligibilityResponseNotEligibleError: (Int, Option[VrmAssignEligibilityResponse]) = {
    (OK,
      Some(VrmAssignEligibilityResponse(certificateExpiryDate = Some(new DateTime().minus(Period.years(2))),
           responseCode = Some("U1122 - vrm_assign_eligibility_failure"))
      )
    )
  }
}