package webserviceclients.fakes

import org.joda.time.DateTime
import org.joda.time.Period
import play.api.http.Status.{FORBIDDEN, OK}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityResponse
import webserviceclients.vrmassigneligibility.VrmAssignEligibilityResponseDto

object VrmAssignEligibilityWebServiceConstants {

  def vrmAssignEligibilityResponseSuccess = {
    (OK, VrmAssignEligibilityResponseDto(None, VrmAssignEligibilityResponse(Some(new DateTime))))
  }

  def vrmAssignEligibilityResponseExpiredCertWithin6Years = {
    (OK,
      VrmAssignEligibilityResponseDto(
        None,
        VrmAssignEligibilityResponse(Some(new DateTime().minusYears(6)))
      )
    )
  }

  def vrmAssignEligibilityResponseExpiredCertOver6Years = {
    (OK,
      VrmAssignEligibilityResponseDto(
        None,
        VrmAssignEligibilityResponse(Some(new DateTime().minusYears(6).minusDays(1)))
      )
    )
  }

  def vrmAssignEligibilityResponseDirectToPaperError = {
    (FORBIDDEN,
      VrmAssignEligibilityResponseDto(
        Some(MicroserviceResponse("", "vrm_assign_eligibility_direct_to_paper")),
        VrmAssignEligibilityResponse(Some(new DateTime().minus(Period.years(2))))
      )
    )
  }

  def vrmAssignEligibilityResponseNotEligibleError = {
    (FORBIDDEN,
      VrmAssignEligibilityResponseDto(
        Some(MicroserviceResponse("", "vrm_assign_eligibility_failure")),
        VrmAssignEligibilityResponse(Some(new DateTime().minus(Period.years(2))))
      )
    )
  }
}
