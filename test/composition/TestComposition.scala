package composition

import com.google.inject.util.Modules
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Module
import composition.addresslookup.TestAddressLookupWebServiceBinding
import composition.webserviceclients.addresslookup.AddressLookupServiceBinding
import composition.webserviceclients.audit2
import composition.webserviceclients.audit2.AuditServiceDoesNothing
import composition.webserviceclients.bruteforceprevention.BruteForcePreventionServiceBinding
import composition.webserviceclients.bruteforceprevention.TestBruteForcePreventionWebServiceBinding
import composition.webserviceclients.emailservice.TestEmailServiceWebServiceBinding
import composition.webserviceclients.paymentsolve.PaymentServiceBinding
import composition.webserviceclients.paymentsolve.TestPaymentWebServiceBinding
import composition.webserviceclients.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebServiceBinding
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupServiceBinding
import composition.webserviceclients.vrmassigneligibility.TestVrmAssignEligibilityWebServiceBinding
import composition.webserviceclients.vrmassigneligibility.VrmAssignEligibilityServiceBinding
import composition.webserviceclients.vrmassignfulfil.TestVrmAssignFulfilWebServiceBinding
import composition.webserviceclients.vrmassignfulfil.VrmAssignFulfilServiceBinding

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