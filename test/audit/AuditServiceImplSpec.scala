package audit

import composition.{TestConfig, WithApplication}
import helpers.UnitSpec
import uk.gov.dvla.auditing.Message

class AuditServiceImplSpec extends UnitSpec {

  "send" should {
    "add message to a queue" in new WithApplication {
      val message = Message(name = "stub-name", serviceType = "stub-service-type")
      auditService.send(message)
      true should equal(true)
    }
  }

  private lazy val auditService = testInjector(
    new TestConfig(
      //auditServiceUseRabbit = true,
      rabbitmqHost = "TODO",
      rabbitmqQueue = "TODO")
  ).
    getInstance(classOf[AuditService])
}
