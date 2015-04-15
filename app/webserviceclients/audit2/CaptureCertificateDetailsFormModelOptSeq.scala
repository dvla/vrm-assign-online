package webserviceclients.audit2

import models.CaptureCertificateDetailsFormModel

object CaptureCertificateDetailsFormModelOptSeq {

  def from(captureCertificateDetailsFormModel: Option[CaptureCertificateDetailsFormModel]) = {
    captureCertificateDetailsFormModel match {
      case Some(captureCertificateDetailsFormModel) => {
        val certificateDocumentCountOpt = Some(("certificateDocumentCount", captureCertificateDetailsFormModel.certificateDocumentCount))
        val certificateDateOpt = Some(("certificateDate", captureCertificateDetailsFormModel.certificateDate))
        val certificateTimeOpt = Some(("certificateTime", captureCertificateDetailsFormModel.certificateTime))
        val certificateRegistrationMarkOpt = Some(("certificateRegistrationMark", captureCertificateDetailsFormModel.certificateRegistrationMark))
        val prVrmOpt = Some(("prVrm", captureCertificateDetailsFormModel.prVrm))
        Seq(certificateDocumentCountOpt, certificateDateOpt, certificateTimeOpt, certificateRegistrationMarkOpt, prVrmOpt)
      }
      case _ => Seq.empty
    }
  }
}
