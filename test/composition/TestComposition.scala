package composition

import com.google.inject.util.Modules
import com.google.inject.{Guice, Injector, Module}
import composition.addresslookup.TestAddressLookupWebServiceBinding
import composition.webserviceclients.addresslookup.AddressLookupServiceBinding
import composition.webserviceclients.audit2
import composition.webserviceclients.audit2.{AuditServiceDoesNothing, AuditServiceBinding}
import composition.webserviceclients.bruteforceprevention.{BruteForcePreventionServiceBinding, TestBruteForcePreventionWebServiceBinding}
import composition.webserviceclients.emailservice.{TestEmailServiceWebServiceBinding, EmailServiceBinding}
import composition.webserviceclients.paymentsolve.{PaymentServiceBinding, TestPaymentWebServiceBinding}
import composition.webserviceclients.vehicleandkeeperlookup.{TestVehicleAndKeeperLookupWebServiceBinding, VehicleAndKeeperLookupServiceBinding}
import composition.webserviceclients.vrmassigneligibility.{TestVrmAssignEligibilityWebServiceBinding, VrmAssignEligibilityServiceBinding}
import composition.webserviceclients.vrmassignfulfil.{VrmAssignFulfilServiceBinding, TestVrmAssignFulfilWebServiceBinding}

trait TestComposition extends Composition {

  override lazy val injector: Injector = testInjector()

  def testInjector(modules: Module*) = {
    val overriddenDevModule = Modules.`override`(
      // Real implementations (but no external calls)
      new AddressLookupServiceBinding,
      new VehicleAndKeeperLookupServiceBinding,
      new CookieFlagsBinding,
      new VrmAssignEligibilityServiceBinding,
      new VrmAssignFulfilServiceBinding,
      new PaymentServiceBinding,
      new SessionFactoryBinding,
      new BruteForcePreventionServiceBinding,
      new LoggerLikeBinding,
      new PdfServiceBinding,
      // Completely mocked web services below...
      new TestConfig,
      new TestAddressLookupWebServiceBinding,
      new TestVehicleAndKeeperLookupWebServiceBinding,
      new TestDateServiceBinding,
      new TestVrmAssignEligibilityWebServiceBinding,
      new TestVrmAssignFulfilWebServiceBinding,
      new TestPaymentWebServiceBinding,
      new TestBruteForcePreventionWebServiceBinding,
      new TestRefererFromHeaderBinding,
      new AuditServiceDoesNothing,
      new audit2.AuditMicroServiceCallNotOk,
      new TestAssignEmailServiceBinding,
      new TestEmailService,
      new TestEmailServiceWebServiceBinding,
      new TestDateTimeZoneServiceBinding
    ).`with`(modules: _*)
    Guice.createInjector(overriddenDevModule)
  }
}