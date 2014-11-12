package models

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import org.joda.time.DateTime
import views.vrm_assign.CaptureCertificateDetails.CaptureCertificateDetailsCacheKey

final case class CaptureCertificateDetailsModel(lastDate: Option[DateTime], outstandingDates: List[String], outstandingFees: Int)

object CaptureCertificateDetailsModel {

  // Create a EligibilityModel from the given lastDate. We do this in order get the data out of the response from micro-service call
  def from(lastDate: Option[DateTime], outstandingDates: List[String], outstandingFees: Int = 0) =
    CaptureCertificateDetailsModel(lastDate, outstandingDates, outstandingFees)

  implicit val JsonFormat = Json.format[CaptureCertificateDetailsModel]
  implicit val Key = CacheKey[CaptureCertificateDetailsModel](CaptureCertificateDetailsCacheKey)
}