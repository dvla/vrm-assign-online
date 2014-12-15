package pdf

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel

import scala.concurrent.Future

trait PdfService {

  def create(transactionId: String, name: String, address: Option[AddressModel], prVrm: String): Future[Array[Byte]]
}