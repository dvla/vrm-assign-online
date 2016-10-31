package webserviceclients.audit2

import models.CaptureCertificateDetailsModel
import models.Certificate.{Expired, ExpiredWithFee, Valid, Unknown}

object CaptureCertificateDetailsModelOptSeq {

  def from(captureCertificateDetailsModel: Option[CaptureCertificateDetailsModel]) = {
    captureCertificateDetailsModel match {
      case Some(certificateDetailsModel) =>
        certificateDetailsModel.certificate match {
          case Expired(expiryDate) => Seq(Some(("certificateExpiryDate", expiryDate.toString)))
          case ExpiredWithFee(expiryDate, fee, _) => Seq(Some(("certificateExpiryDate", expiryDate.toString)),
            Some(("outstandingFees", fee)))
          case Valid(expiryDate) => Seq(Some(("certificateExpiryDate", expiryDate.toString)))
          case Unknown => Seq.empty
        }
      case _ => Seq.empty
    }
  }
}
