package composition

import com.google.inject.util.Modules
import com.google.inject.{Guice, Injector, Module}
import composition.addresslookup.TestAddressLookupWebServiceBinding
import composition.audit1.AuditLocalServiceDoesNothingBinding
import composition.webserviceclients.addresslookup.AddressLookupServiceBinding
import composition.webserviceclients.audit2
import composition.webserviceclients.audit2.{AuditServiceDoesNothing, AuditServiceBinding}
import composition.webserviceclients.bruteforceprevention.{BruteForcePreventionServiceBinding, TestBruteForcePreventionWebServiceBinding}
import composition.webserviceclients.emailservice.EmailServiceBinding
import composition.webserviceclients.paymentsolve.{PaymentServiceBinding, TestPaymentWebServiceBinding}
import composition.webserviceclients.vehicleandkeeperlookup.{TestVehicleAndKeeperLookupWebServiceBinding, VehicleAndKeeperLookupServiceBinding}
import composition.webserviceclients.vrmassigneligibility.{TestVrmAssignEligibilityWebServiceBinding, VrmAssignEligibilityServiceBinding}
import composition.webserviceclients.vrmassignfulfil.{VrmAssignFulfilServiceBinding, VrmAssignFulfilWebServiceBinding}

trait TestComposition extends Composition {

  override lazy val injector: Injector = testInjector()

  def testInjector(modules: Module*) = {
    val overriddenDevModule = Modules.`override`(
      // Real implementations (but no external calls)
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
      new PdfServiceBinding,
      new AssignEmailServiceBinding,
      new EmailServiceBinding,
      // Completely mocked web services below...
      new TestConfig,
      new TestAddressLookupWebServiceBinding,
      new TestVehicleAndKeeperLookupWebServiceBinding,
      new TestDateServiceBinding,
      new TestVrmAssignEligibilityWebServiceBinding,
      //  VrmAssignFulfilWebService, // TODO there should be a stubbed version of this web service!
      new TestPaymentWebServiceBinding,
      new TestBruteForcePreventionWebServiceBinding,
      new TestRefererFromHeaderBinding,
      new AuditLocalServiceDoesNothingBinding,
      new AuditServiceDoesNothing,
      new audit2.AuditMicroServiceCallNotOk
    ).`with`(modules: _*)
    Guice.createInjector(overriddenDevModule)
  }
}