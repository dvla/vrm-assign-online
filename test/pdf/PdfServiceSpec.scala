package pdf

import helpers.WithApplication
import helpers.UnitSpec
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.Seconds
import org.scalatest.time.Span
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.TransactionIdValid

class PdfServiceSpec extends UnitSpec {

  // See getting started documentation from https://pdfbox.apache.org/cookbook/documentcreation.html

  // See http://stackoverflow.com/questions/13917105/how-to-download-a-file-with-play-framework-2-0
  // for how to do the controller.

  "create" should {
    "return a non-empty output stream" in new WithApplication {
      val pdf = pdfService.create(
        transactionId = TransactionIdValid,
        name = "stub name",
        address = None,
        prVrm = RegistrationNumberValid,
        trackingId = TrackingId("")
      )

      pdf should not equal null
      pdf.length > 0 should equal(true)
    }
  }

  private def pdfService = testInjector().getInstance(classOf[PdfService])

  private def longTimeout = Timeout(Span(30, Seconds))
}
