package webserviceclients.audit2

import models.CaptureCertificateDetailsModel

object CaptureCertificateDetailsModelOptSeq {

  def from(captureCertificateDetailsModel: Option[CaptureCertificateDetailsModel]) = {
    captureCertificateDetailsModel match {
      case Some(certificateDetailsModel) =>
        val certificateExpiryDateOpt = certificateDetailsModel.certificateExpiryDate.map(
          expiryDate => ("certificateExpiryDate", expiryDate.toString))
        val outstandingFeesOpt = Some(("outstandingFees", certificateDetailsModel.outstandingFees))
        Seq(certificateExpiryDateOpt, outstandingFeesOpt)
      case _ => Seq.empty
    }
  }
}
