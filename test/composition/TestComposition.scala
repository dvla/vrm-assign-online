package composition

import _root_.webserviceclients.paymentsolve.{PaymentSolveService, PaymentSolveServiceImpl, PaymentSolveWebService}
import _root_.webserviceclients.vrmassignfulfil.{VrmAssignFulfilService, VrmAssignFulfilServiceImpl, VrmAssignFulfilWebService, VrmAssignFulfilWebServiceImpl}
import _root_.webserviceclients.vrmretentioneligibility.{VrmAssignEligibilityService, VrmAssignEligibilityServiceImpl, VrmAssignEligibilityWebService, VrmAssignEligibilityWebServiceImpl}
import com.google.inject.name.Names
import com.google.inject.util.Modules
import com.google.inject.{Guice, Injector, Module}
import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.addresslookup.AddressLookupServiceBinding
import composition.webserviceclients.paymentsolve.TestPaymentSolveWebService
import composition.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperLookupServiceBinding, TestVehicleAndKeeperLookupWebServiceBinding, TestVehicleAndKeeperLookupWebServiceBinding$}
import email.{EmailService, EmailServiceImpl}
import org.scalatest.mock.MockitoSugar
import pdf.{PdfService, PdfServiceImpl}
import play.api.{Logger, LoggerLike}
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession._
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter._
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.{BruteForcePreventionService, BruteForcePreventionServiceImpl, BruteForcePreventionWebService}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperLookupService, VehicleAndKeeperLookupServiceImpl, VehicleAndKeeperLookupWebService}
import utils.helpers.AssignCookieFlags

trait TestComposition extends Composition {

  override lazy val injector: Injector = testInjector()

  def testInjector(modules: Module*) = {
    val overriddenDevModule = Modules.`override`(
      // Real implementations (but no external calls)
      new TestModule(),
      new AddressLookupServiceBinding,
      new VehicleAndKeeperLookupServiceBinding,
      // Completely mocked web services below...
      new TestConfig,
      new TestAddressLookupWebServiceBinding,
      new TestVehicleAndKeeperLookupWebServiceBinding
    ).`with`(modules: _*)
    Guice.createInjector(overriddenDevModule)
  }
}

final class TestModule extends ScalaModule with MockitoSugar {

  def configure() {
    bind[VehicleAndKeeperLookupService].to[VehicleAndKeeperLookupServiceImpl].asEagerSingleton()
    bind[DateService].toInstance(new TestDateService().build())
    bind[CookieFlags].to[AssignCookieFlags].asEagerSingleton()
    bind[VrmAssignEligibilityWebService].to[VrmAssignEligibilityWebServiceImpl].asEagerSingleton()
    bind[VrmAssignEligibilityService].to[VrmAssignEligibilityServiceImpl].asEagerSingleton()
    bind[VrmAssignFulfilWebService].to[VrmAssignFulfilWebServiceImpl].asEagerSingleton()
    bind[VrmAssignFulfilService].to[VrmAssignFulfilServiceImpl].asEagerSingleton()
    bind[PaymentSolveWebService].toInstance(new TestPaymentSolveWebService().build())
    bind[PaymentSolveService].to[PaymentSolveServiceImpl].asEagerSingleton()

    bindSessionFactory()

    bind[BruteForcePreventionWebService].toInstance(new TestBruteForcePreventionWebService().build())
    bind[BruteForcePreventionService].to[BruteForcePreventionServiceImpl].asEagerSingleton()
    bind[LoggerLike].annotatedWith(Names.named(AccessLoggerName)).toInstance(Logger("dvla.common.AccessLogger"))
    bind[PdfService].to[PdfServiceImpl].asEagerSingleton()
    bind[EmailService].to[EmailServiceImpl].asEagerSingleton()
    bind[audit1.AuditService].toInstance(new composition.audit1Mock.AuditLocalServiceBinding().build())
    bind[RefererFromHeader].toInstance(new TestRefererFromHeader().build)
    bind[_root_.webserviceclients.audit2.AuditMicroService].to[_root_.webserviceclients.audit2.AuditMicroServiceImpl].asEagerSingleton()
    bind[_root_.webserviceclients.audit2.AuditService].toInstance(new composition.webserviceclients.audit2.AuditServiceDoesNothing().build())
  }

  protected def bindSessionFactory(): Unit = {
    if (getOptionalProperty[Boolean]("encryptCookies").getOrElse(true)) {
      bind[CookieEncryption].toInstance(new AesEncryption with CookieEncryption)
      bind[CookieNameHashGenerator].toInstance(new Sha1HashGenerator with CookieNameHashGenerator)
      bind[ClientSideSessionFactory].to[EncryptedClientSideSessionFactory].asEagerSingleton()
    } else
      bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()
  }
}