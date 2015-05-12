package views.vrm_assign

import models.CacheKeyPrefix

object CaptureCertificateDetails {

  final val CertificateDateId = "certificate-date"
  final val CertificateTimeId = "certificate-time"
  final val CertificateDocumentCountId = "certificate-document-count"
  final val CertificateRegistrationMarkId = "certificate-registration-mark"
  final val CaptureCertificateDetailsCacheKey = s"${CacheKeyPrefix}capture-certificate-details"
  final val CaptureCertificateDetailsFormModelCacheKey = s"${CacheKeyPrefix}capture-certificate-details-form-model"
  final val SubmitId = "submit"
  final val SubmitName = "action"
  final val ExitId = "exit"
}