package webserviceclients.fakes

import org.joda.time.DateTime
import models.Certificate.{ExpiredWithFee, Valid}

object CaptureCertificateDetailsWebServiceConstants {

  final val ValidCertificate = Valid(DateTime.now)
  final val ExpiredWithFeeCertificate = ExpiredWithFee(DateTime.now.minusDays(1), 17000, "170.00")
}
