package controllers

import helpers.UnitSpec
import models.CaptureCertificateDetailsFormModel
import play.api.data.Form
import views.vrm_assign.CaptureCertificateDetails.CertificateDateId
import views.vrm_assign.CaptureCertificateDetails.CertificateDocumentCountId
import views.vrm_assign.CaptureCertificateDetails.CertificateRegistrationMarkId
import views.vrm_assign.CaptureCertificateDetails.CertificateTimeId
import views.vrm_assign.CaptureCertificateDetails.PrVrmId
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateDateValid
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateDocumentCountValid
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateRegistrationMarkValid
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateTimeValid
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.PrVrmValid

final class CaptureCertificateDetailsFormSpec extends UnitSpec {

  "form" should {

    "accept when all fields contain valid responses" in {
      formWithValidDefaults().get.certificateDate should equal(CertificateDateValid)
    }

    "reject when empty" in {
      val result = formWithValidDefaults(
        certificateDate = "",
        certificateDocumentCount = "",
        certificateRegistrationMark = "",
        certificateTime = "",
        prVrm = ""
      )
      result.hasErrors should equal(true)
    }

    "reject when certificate-document-count is blank" in {
      val errors = formWithValidDefaults(
        certificateDocumentCount = ""
      ).errors
      errors should have length 3
      errors(0).key should equal(CertificateDocumentCountId)
      errors(0).message should equal("error.minLength")
      errors(1).key should equal(CertificateDocumentCountId)
      errors(1).message should equal("error.required")
      errors(2).key should equal(CertificateDocumentCountId)
      errors(2).message should equal("error.validCertificateDocument")
    }

    "reject when certificate-document-count has too many characters" in {
      val errors = formWithValidDefaults(
        certificateDocumentCount = "9" * 20
      ).errors
      errors should have length 2
      errors(0).key should equal(CertificateDocumentCountId)
      errors(0).message should equal("error.maxLength")
      errors(1).key should equal(CertificateDocumentCountId)
      errors(1).message should equal("error.validCertificateDocument")
    }

    "reject when certificate-document-count has invalid characters" in {
      val errors = formWithValidDefaults(
        certificateDocumentCount = "?"
      ).errors
      errors should have length 1
      errors(0).key should equal(CertificateDocumentCountId)
      errors(0).message should equal("error.validCertificateDocument")
    }

    "reject when certificate-date is blank" in {
      val errors = formWithValidDefaults(
        certificateDate = ""
      ).errors
      errors should have length 3
      errors(0).key should equal(CertificateDateId)
      errors(0).message should equal("error.minLength")
      errors(1).key should equal(CertificateDateId)
      errors(1).message should equal("error.required")
      errors(2).key should equal(CertificateDateId)
      errors(2).message should equal("error.validCertificateDate")
    }

    "reject when certificate-date has too few characters" in {
      val errors = formWithValidDefaults(
        certificateDate = "9"
      ).errors
      errors should have length 1
      errors(0).key should equal(CertificateDateId)
      errors(0).message should equal("error.minLength")
    }

    "reject when certificate-date has too many characters" in {
      val errors = formWithValidDefaults(
        certificateDate = "9" * 20
      ).errors
      errors should have length 1
      errors(0).key should equal(CertificateDateId)
      errors(0).message should equal("error.maxLength")
    }

    "reject when certificate-registration-mark is blank" in {
      val errors = formWithValidDefaults(
        certificateRegistrationMark = ""
      ).errors
      errors should have length 3
      errors(0).key should equal(CertificateRegistrationMarkId)
      errors(0).message should equal("error.minLength")
      errors(1).key should equal(CertificateRegistrationMarkId)
      errors(1).message should equal("error.required")
      errors(2).key should equal(CertificateRegistrationMarkId)
      errors(2).message should equal("error.validCertificateRegistrationMark")
    }

    "reject when certificate-time is blank" in {
      val errors = formWithValidDefaults(
        certificateTime = ""
      ).errors
      errors should have length 3
      errors(0).key should equal(CertificateTimeId)
      errors(0).message should equal("error.minLength")
      errors(1).key should equal(CertificateTimeId)
      errors(1).message should equal("error.required")
      errors(2).key should equal(CertificateTimeId)
      errors(2).message should equal("error.validCertificateTime")
    }

    "reject when pr-vrm is blank" in {
      val errors = formWithValidDefaults(
        prVrm = ""
      ).errors
      errors should have length 3
      errors(0).key should equal(PrVrmId)
      errors(0).message should equal("error.minLength")
      errors(1).key should equal(PrVrmId)
      errors(1).message should equal("error.required")
      errors(2).key should equal(PrVrmId)
      errors(2).message should equal("error.restricted.validVrnOnly")
    }
  }

  private def formWithValidDefaults(
                                     certificateDate: String = CertificateDateValid,
                                     certificateDocumentCount: String = CertificateDocumentCountValid,
                                     certificateRegistrationMark: String = CertificateRegistrationMarkValid,
                                     certificateTime: String = CertificateTimeValid,
                                     prVrm: String = PrVrmValid
                                     ) = {
    Form(CaptureCertificateDetailsFormModel.Form.Mapping).bind(
      Map(
        CertificateDateId -> certificateDate,
        CertificateDocumentCountId -> certificateDocumentCount,
        CertificateRegistrationMarkId -> certificateRegistrationMark,
        CertificateTimeId -> certificateTime,
        PrVrmId -> prVrm
      )
    )
  }
}