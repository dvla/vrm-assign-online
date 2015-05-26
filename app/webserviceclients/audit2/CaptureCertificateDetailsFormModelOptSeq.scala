package webserviceclients.audit2

import models.CaptureCertificateDetailsFormModel

object CaptureCertificateDetailsFormModelOptSeq {

  def from(captureCertificateDetailsFormModel: Option[CaptureCertificateDetailsFormModel]) = {
    captureCertificateDetailsFormModel match {
      case Some(model) =>
        val certificateDocumentCountOpt = Some(("certificateDocumentCount", model.certificateDocumentCount))
        val certificateDateOpt = Some(("certificateDate", model.certificateDate))
        val certificateTimeOpt = Some(("certificateTime", model.certificateTime))
        val certificateRegistrationMarkOpt = Some(("certificateRegistrationMark", model.certificateRegistrationMark))
        Seq(certificateDocumentCountOpt, certificateDateOpt, certificateTimeOpt, certificateRegistrationMarkOpt)
      case _ => Seq.empty
    }
  }
}
