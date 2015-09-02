package models

import mappings.common.vrm_assign.{CertificateTime, CertificateDate, CertificateDocumentCount}
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.VehicleRegistrationNumber
import views.vrm_assign.CaptureCertificateDetails.CaptureCertificateDetailsFormModelCacheKey
import views.vrm_assign.CaptureCertificateDetails.CertificateDocumentCountId
import views.vrm_assign.CaptureCertificateDetails.CertificateDateId
import views.vrm_assign.CaptureCertificateDetails.CertificateTimeId
import views.vrm_assign.CaptureCertificateDetails.CertificateRegistrationMarkId

/**
 * The certificate capture model.
 * It captures the information of the V750 Certificate of Entitlement or the V778 Retention Document
 * @param certificateDocumentCount the 1st box
 * @param certificateDate the 2nd box
 * @param certificateTime the 3rd box
 * @param certificateRegistrationMark the 4th box
 */
final case class CaptureCertificateDetailsFormModel(certificateDocumentCount: String,
                                                    certificateDate: String,
                                                    certificateTime: String,
                                                    certificateRegistrationMark: String)

object CaptureCertificateDetailsFormModel {

  implicit val JsonFormat = Json.format[CaptureCertificateDetailsFormModel]
  implicit val Key = CacheKey[CaptureCertificateDetailsFormModel](CaptureCertificateDetailsFormModelCacheKey)

  /**
   * Mapping from/to a model and a play framework Form
   */
  object Form {

    final val Mapping = mapping(
      CertificateDocumentCountId -> CertificateDocumentCount.certificateDocumentCountMapping,
      CertificateDateId -> CertificateDate.certificateDateMapping,
      CertificateTimeId -> CertificateTime.certificateTimeMapping,
      CertificateRegistrationMarkId -> VehicleRegistrationNumber.registrationNumber
    )(CaptureCertificateDetailsFormModel.apply)(CaptureCertificateDetailsFormModel.unapply)
  }
}