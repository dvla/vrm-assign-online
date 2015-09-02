package pdf

import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel

trait PdfService extends DVLALogger {

  def create(transactionId: String, name: String,
             address: Option[AddressModel], prVrm: String, trackingId: TrackingId): Array[Byte]
}