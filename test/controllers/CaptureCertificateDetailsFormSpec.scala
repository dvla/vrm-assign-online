package controllers

import helpers.UnitSpec
import mappings.common.vrm_assign.CertificateDate
import mappings.common.vrm_assign.CertificateDocumentCount
import mappings.common.vrm_assign.CertificateTime
import models.CaptureCertificateDetailsFormModel
import play.api.data.Form
import uk.gov.dvla.vehicles.presentation.common.mappings.VehicleRegistrationNumber
import views.vrm_assign.CaptureCertificateDetails.CertificateDateId
import views.vrm_assign.CaptureCertificateDetails.CertificateDocumentCountId
import views.vrm_assign.CaptureCertificateDetails.CertificateRegistrationMarkId
import views.vrm_assign.CaptureCertificateDetails.CertificateTimeId
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateDateValid
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateDocumentCountValid
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateTimeValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid

class CaptureCertificateDetailsFormSpec extends UnitSpec {

  "form" should {
    "accept when all fields contain valid responses" in {
      formWithValidDefaults().get.certificateDate should equal(CertificateDateValid)
    }

    "reject when empty" in {
      val result = formWithValidDefaults(
        certificateDate = "",
        certificateDocumentCount = "",
        certificateRegistrationMark = "",
        certificateTime = ""
      )
      result.hasErrors should equal(true)
    }

    "reject when certificate-document-count is blank" in {
      val errors = formWithValidDefaults(
        certificateDocumentCount = ""
      ).errors
      errors should have length 3
      errors.head.key should equal(CertificateDocumentCountId)
      errors.head.message should equal("error.minLength")
      errors(1).key should equal(CertificateDocumentCountId)
      errors(1).message should equal("error.required")
      errors(2).key should equal(CertificateDocumentCountId)
      errors(2).message should equal("error.validCertificateDocument")
    }

    "reject when certificate-document-count has too many characters" in {
      val errors = formWithValidDefaults(
        certificateDocumentCount = "9" * (CertificateDocumentCount.MaxLength + 1)
      ).errors
      errors should have length 2
      errors.head.key should equal(CertificateDocumentCountId)
      errors.head.message should equal("error.maxLength")
      errors(1).key should equal(CertificateDocumentCountId)
      errors(1).message should equal("error.validCertificateDocument")
    }

    "reject when certificate-document-count has invalid characters" in {
      val errors = formWithValidDefaults(
        certificateDocumentCount = "?" * CertificateDocumentCount.MinLength
      ).errors
      errors should have length 1
      errors.head.key should equal(CertificateDocumentCountId)
      errors.head.message should equal("error.validCertificateDocument")
    }

    "reject when certificate-date is blank" in {
      val errors = formWithValidDefaults(
        certificateDate = ""
      ).errors
      errors should have length 2
      errors.head.key should equal(CertificateDateId)
      errors.head.message should equal("error.minLength")
      errors(1).key should equal(CertificateDateId)
      errors(1).message should equal("error.required")
    }

    "reject when certificate-date has too few characters" in {
      val errors = formWithValidDefaults(
        certificateDate = "9" * (CertificateDate.MinLength - 1)
      ).errors
      errors should have length 1
      errors.head.key should equal(CertificateDateId)
      errors.head.message should equal("error.minLength")
    }

    "reject when certificate-date has too many characters" in {
      val errors = formWithValidDefaults(
        certificateDate = "9" * (CertificateDate.MaxLength + 1)
      ).errors
      errors should have length 1
      errors.head.key should equal(CertificateDateId)
      errors.head.message should equal("error.maxLength")
    }

    "reject when certificate-date has invalid characters" in {
      val errors = formWithValidDefaults(
        certificateDate = "?" * CertificateDate.MinLength
      ).errors
      errors should have length 1
      errors.head.key should equal(CertificateDateId)
      errors.head.message should equal("error.validCertificateDate")
    }

    "reject when certificate-registration-mark is blank" in {
      val errors = formWithValidDefaults(
        certificateRegistrationMark = ""
      ).errors
      errors should have length 3
      errors.head.key should equal(CertificateRegistrationMarkId)
      errors.head.message should equal("error.minLength")
      errors(1).key should equal(CertificateRegistrationMarkId)
      errors(1).message should equal("error.required")
      errors(2).key should equal(CertificateRegistrationMarkId)
      errors(2).message should equal("error.restricted.validVrnOnly")
    }

    "reject when certificate-registration-mark has too few characters" in {
      val errors = formWithValidDefaults(
        certificateRegistrationMark = "a"
      ).errors
      errors should have length 2
      errors.head.key should equal(CertificateRegistrationMarkId)
      errors.head.message should equal("error.minLength")
      errors(1).key should equal(CertificateRegistrationMarkId)
      errors(1).message should equal("error.restricted.validVrnOnly")
    }

    "reject when certificate-registration-mark has too many characters" in {
      val errors = formWithValidDefaults(
        certificateRegistrationMark = "9" * (VehicleRegistrationNumber.MaxLength + 1)
      ).errors
      errors should have length 2
      errors.head.key should equal(CertificateRegistrationMarkId)
      errors.head.message should equal("error.maxLength")
      errors(1).key should equal(CertificateRegistrationMarkId)
      errors(1).message should equal("error.restricted.validVrnOnly")
    }

    "reject when certificate-registration-mark has invalid characters" in {
      val errors = formWithValidDefaults(
        certificateRegistrationMark = "?" * VehicleRegistrationNumber.MinLength
      ).errors
      errors should have length 1
      errors.head.key should equal(CertificateRegistrationMarkId)
      errors.head.message should equal("error.restricted.validVrnOnly")
    }

    "reject when certificate-time is blank" in {
      val errors = formWithValidDefaults(
        certificateTime = ""
      ).errors
      errors should have length 2
      errors.head.key should equal(CertificateTimeId)
      errors.head.message should equal("error.minLength")
      errors(1).key should equal(CertificateTimeId)
      errors(1).message should equal("error.required")
    }

    "accept when certificate-time has too few characters" in {
      formWithValidDefaults(
        certificateTime = "1" * (CertificateTime.MaxLength - 1)
      ).get.certificateTime should equal("011111")
    }

    "reject when certificate-time has too many characters" in {
      val errors = formWithValidDefaults(
        certificateTime = "9" * (CertificateTime.MaxLength + 1)
      ).errors
      errors should have length 1
      errors.head.key should equal(CertificateTimeId)
      errors.head.message should equal("error.maxLength")
    }

    "reject when certificate-time has invalid characters" in {
      val errors = formWithValidDefaults(
        certificateTime = "?" * CertificateTime.MinLength
      ).errors
      errors should have length 1
      errors.head.key should equal(CertificateTimeId)
      errors.head.message should equal("error.validCertificateTime")
    }

  }

  private def formWithValidDefaults(
                                     certificateDate: String = CertificateDateValid,
                                     certificateDocumentCount: String = CertificateDocumentCountValid,
                                     certificateRegistrationMark: String = RegistrationNumberValid,
                                     certificateTime: String = CertificateTimeValid) = {
    Form(CaptureCertificateDetailsFormModel.Form.Mapping).bind(
      Map(
        CertificateDateId -> certificateDate,
        CertificateDocumentCountId -> certificateDocumentCount,
        CertificateRegistrationMarkId -> certificateRegistrationMark,
        CertificateTimeId -> certificateTime
      )
    )
  }
}