package composition

import com.google.inject.name.Names
import com.google.inject.util.Modules
import com.google.inject.{Guice, Injector, Module}
import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.addresslookup.AddressLookupServiceBinding
import composition.webserviceclients.bruteforceprevention.BruteForcePreventionServiceBinding
import composition.webserviceclients.paymentsolve.{PaymentServiceBinding, TestPaymentWebServiceBinding}
import composition.webserviceclients.vehicleandkeeperlookup.{TestVehicleAndKeeperLookupWebServiceBinding, VehicleAndKeeperLookupServiceBinding}
import composition.webserviceclients.vrmassigneligibility.{TestVrmAssignEligibilityWebService, VrmAssignEligibilityServiceBinding}
import composition.webserviceclients.vrmassignfulfil.{VrmAssignFulfilServiceBinding, VrmAssignFulfilWebServiceBinding}
import email.{EmailService, EmailServiceImpl}
import org.scalatest.mock.MockitoSugar
import pdf.{PdfService, PdfServiceImpl}
import play.api.{Logger, LoggerLike}
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.{BruteForcePreventionService, BruteForcePreventionServiceImpl}

trait TestComposition extends Composition {

  override lazy val injector: Injector = testInjector()

  def testInjector(modules: Module*) = {
    val overriddenDevModule = Modules.`override`(
      // Real implementations (but no external calls)
      new TestModule(),
      new AddressLookupServiceBinding,
      new VehicleAndKeeperLookupServiceBinding,
      new CookieFlagsBinding,
      new VrmAssignEligibilityServiceBinding,
      new VrmAssignFulfilWebServiceBinding,
      new VrmAssignFulfilServiceBinding,
      new PaymentServiceBinding,
      new SessionFactoryBinding,
      new BruteForcePreventionServiceBinding,
      new LoggerLikeBinding,
      // Completely mocked web services below...
      new TestConfig,
      new TestAddressLookupWebServiceBinding,
      new TestVehicleAndKeeperLookupWebServiceBinding,
      new TestDateService,
      new TestVrmAssignEligibilityWebService,
      //  VrmAssignFulfilWebService, // TODO there should be a stubbed version of this web service!
      new TestPaymentWebServiceBinding,
      new TestBruteForcePreventionWebService
    ).`with`(modules: _*)
    Guice.createInjector(overriddenDevModule)
  }
}

final class TestModule extends ScalaModule with MockitoSugar {

  def configure() {
    bind[BruteForcePreventionService].to[BruteForcePreventionServiceImpl].asEagerSingleton()
    bind[PdfService].to[PdfServiceImpl].asEagerSingleton()
    bind[EmailService].to[EmailServiceImpl].asEagerSingleton()
    bind[audit1.AuditService].toInstance(new composition.audit1Mock.AuditLocalServiceBinding().build())
    bind[RefererFromHeader].toInstance(new TestRefererFromHeader().build)
    bind[_root_.webserviceclients.audit2.AuditMicroService].to[_root_.webserviceclients.audit2.AuditMicroServiceImpl].asEagerSingleton()
    bind[_root_.webserviceclients.audit2.AuditService].toInstance(new composition.webserviceclients.audit2.AuditServiceDoesNothing().build())
  }
}