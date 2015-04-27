package models

import mappings.common.vrm_assign._
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.VehicleRegistrationNumber
import views.vrm_assign.CaptureCertificateDetails._

final case class CaptureCertificateDetailsFormModel(certificateDocumentCount: String,
                                                    certificateDate: String,
                                                    certificateTime: String,
                                                    certificateRegistrationMark: String,
                                                    prVrm: String)

object CaptureCertificateDetailsFormModel {

  implicit val JsonFormat = Json.format[CaptureCertificateDetailsFormModel]
  implicit val Key = CacheKey[CaptureCertificateDetailsFormModel](CaptureCertificateDetailsFormModelCacheKey)

  object Form {

    final val Mapping = mapping(
      CertificateDocumentCountId -> CertificateDocumentCount.certificateDocumentCountMapping,
      CertificateDateId -> CertificateDate.certificateDateMapping,
      CertificateTimeId -> CertificateTime.certificateTimeMapping,
      CertificateRegistrationMarkId -> VehicleRegistrationNumber.registrationNumber,
      PrVrmId -> VehicleRegistrationNumber.registrationNumber
    )(CaptureCertificateDetailsFormModel.apply)(CaptureCertificateDetailsFormModel.unapply)
  }

}