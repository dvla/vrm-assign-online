package webserviceclients.fakes

import org.joda.time.DateTime
import org.joda.time.Period
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityResponse
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityResponseDto

object VrmAssignEligibilityWebServiceConstants {

  def vrmAssignEligibilityResponseSuccess = {
    (OK, VrmAssignEligibilityResponseDto(None, VrmAssignEligibilityResponse(Some(new DateTime))))
  }

  def vrmAssignEligibilityResponseDirectToPaperError = {
    (INTERNAL_SERVER_ERROR,
      VrmAssignEligibilityResponseDto(
        Some(MicroserviceResponse("A5096", "vrm_assign_eligibility_direct_to_paper")),
        VrmAssignEligibilityResponse(Some(new DateTime().minus(Period.years(2))))
      )
    )
  }

  def vrmAssignEligibilityResponseNotEligibleError = {
    (INTERNAL_SERVER_ERROR,
      VrmAssignEligibilityResponseDto(
        Some(MicroserviceResponse("U1122", "vrm_assign_eligibility_failure")),
        VrmAssignEligibilityResponse(Some(new DateTime().minus(Period.years(2))))
      )
    )
  }
}
