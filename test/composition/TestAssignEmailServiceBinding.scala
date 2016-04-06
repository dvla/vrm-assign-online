package composition

import com.tzavellas.sse.guice.ScalaModule
import email.AssignEmailService
import models.BusinessDetailsModel
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.ConfirmFormModel
import models.VehicleAndKeeperLookupFormModel
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito.when
import org.mockito.Matchers.any
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

final class TestAssignEmailServiceBinding extends ScalaModule with MockitoSugar {

  val stub = mock[AssignEmailService]
  when(stub.emailRequest(
    any[String],
    any[VehicleAndKeeperDetailsModel],
    any[CaptureCertificateDetailsFormModel],
    any[CaptureCertificateDetailsModel],
    any[VehicleAndKeeperLookupFormModel],
    any[String],
    any[String],
    any[Option[ConfirmFormModel]],
    any[Option[BusinessDetailsModel]],
    any[Boolean],
    any[Boolean],
    any[TrackingId]
  )).thenReturn(None)

  def configure() = {
    bind[AssignEmailService].toInstance(stub)
  }
}