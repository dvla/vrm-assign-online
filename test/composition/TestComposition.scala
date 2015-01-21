package composition

import com.google.inject.util.Modules
import com.google.inject.{Guice, Injector, Module}
import composition.paymentsolvewebservice.TestPaymentSolveWebService
import composition.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebService


trait TestComposition extends Composition {

  override lazy val injector: Injector = testInjector(
    new TestBruteForcePreventionWebService,
    new TestDateService,
    new TestOrdnanceSurvey,
    new TestVehicleAndKeeperLookupWebService,
    new TestRefererFromHeader,
    new TestPaymentSolveWebService,
    new TestAuditService
  )

  def testInjector(modules: Module*) = {
    val overriddenDevModule = Modules.`override`(new DevModule()).`with`(modules: _*)
    Guice.createInjector(overriddenDevModule)
  }
}