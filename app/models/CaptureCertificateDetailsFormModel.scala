package models

import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import views.vrm_assign.CaptureCertificateDetails._
import uk.gov.dvla.vehicles.presentation.common.mappings.VehicleRegistrationNumber
import mappings.common.vrm_assign._

final case class CaptureCertificateDetailsFormModel(referenceNumber: String, // TODO remove this when html has been changed
                                                    certificateDate: String,
                                                    certificateTime: String,
                                                    certificateDocumentCount: String,
                                                    certificateRegistrationMark: String,
                                                    prVrm: String)

object CaptureCertificateDetailsFormModel {

  implicit val JsonFormat = Json.format[CaptureCertificateDetailsFormModel]
  implicit val Key = CacheKey[CaptureCertificateDetailsFormModel](CaptureCertificateDetailsFormModelCacheKey)

  object Form {

    final val Mapping = mapping(
      ReferenceNumberId -> ReferenceNumber.referenceNumberMapping, // TODO remove this when html has been changed
      CertificateDateId -> CertificateDate.certificateDateMapping,
      CertificateTimeId -> CertificateTime.certificateTimeMapping,
      CertificateDocumentCountId -> CertificateDocumentCount.certificateDocumentCountMapping,
      CertificateRegistrationMarkId -> CertificateRegistrationMark.certificateRegistrationMarkMapping,
      PrVrmId -> VehicleRegistrationNumber.registrationNumber
    )(CaptureCertificateDetailsFormModel.apply)(CaptureCertificateDetailsFormModel.unapply)
  }
}