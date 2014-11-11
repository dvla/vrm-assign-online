package models

import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import views.vrm_assign.CaptureCertificateDetails._
import uk.gov.dvla.vehicles.presentation.common.mappings.VehicleRegistrationNumber
import mappings.common.vrm_assign.ReferenceNumber

final case class CaptureCertificateDetailsFormModel(referenceNumber: String, prVrm: String)

object CaptureCertificateDetailsFormModel {

  implicit val JsonFormat = Json.format[CaptureCertificateDetailsFormModel]
  implicit val Key = CacheKey[CaptureCertificateDetailsFormModel](CaptureCertificateDetailsFormModelCacheKey)

  object Form {

    final val Mapping = mapping(
      ReferenceNumberId -> ReferenceNumber.referenceNumberMapping,
      PrVrmId -> VehicleRegistrationNumber.registrationNumber
    )(CaptureCertificateDetailsFormModel.apply)(CaptureCertificateDetailsFormModel.unapply)
  }
}