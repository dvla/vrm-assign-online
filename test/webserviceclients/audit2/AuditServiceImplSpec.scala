package webserviceclients.audit2

import composition.webserviceclients.audit2.{AuditMicroServiceCallFails, AuditMicroServiceCallNotOk, AuditServiceBinding}
import composition.{TestConfig, WithApplication}
import helpers.UnitSpec
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Await

final class AuditServiceImplSpec extends UnitSpec with MockitoSugar {

  "invoke" should {

    "re-throw exception when micro-service response returns an exception" in new WithApplication {
      a[RuntimeException] must be thrownBy Await.result(auditMicorServiceCallFails.send(request), finiteTimeout)
    }

    "throw when micro-service response status is not Ok" in new WithApplication {
      a[RuntimeException] must be thrownBy Await.result(auditMicroServiceCallNotOk.send(request), finiteTimeout)
    }
  }

  private def request = mock[AuditRequest]

  private def auditMicorServiceCallFails = testInjector(new AuditMicroServiceCallFails, new AuditServiceBinding).getInstance(classOf[webserviceclients.audit2.AuditService])

  private def auditMicroServiceCallNotOk = testInjector(new AuditMicroServiceCallNotOk, new AuditServiceBinding).getInstance(classOf[webserviceclients.audit2.AuditService])
}