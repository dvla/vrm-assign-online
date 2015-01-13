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

//trait TestComposition extends Composition {
//
//  override lazy val injector: Injector = Guice.createInjector(testMod)
//
//  private def testMod = Modules.`override`(new DevModule {
//    override def bindSessionFactory() = ()
//  }).`with`(new TestModule)
//
//  def testModule(module: Module*) = Modules.`override`(new DevModule()).`with`(module: _*)
//
//  def testInjector(module: Module*) = Guice.createInjector(testModule(module: _*))
//}
//
//private class TestModule() extends ScalaModule with MockitoSugar {
//  /**
//   * Bind the fake implementations the traits
//   */
//  def configure() {
//    Logger.debug("Guice is loading TestModule")
//
//    new TestConfig().configure()
//    new TestDateService().configure()
//    //    bind[Config].toInstance(new TestConfig.configure())
//
//    ordnanceSurveyAddressLookup()
//    //    bind[OrdnanceSurvey].to[TestOrdnanceSurvey].asEagerSingleton()
//
//    //bind[DateService].to[TestDateService].asEagerSingleton()
//    bind[DateTimeZoneService].toInstance(new DateTimeZoneServiceImpl)
//    bind[CookieFlags].to[NoCookieFlags].asEagerSingleton()
//    bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()
//
//    new TestBruteForcePreventionWebService().configure()
//    new TestVehicleAndKeeperLookupWebService().configure()
//    new TestRefererFromHeader().configure()
//    new TestPaymentSolveWebService().configure()
//    new TestAuditService().configure()
//
//
//  }
//
//  private def ordnanceSurveyAddressLookup() = {
//    bind[AddressLookupService].to[uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl]
//
//    val fakeWebServiceImpl = new FakeAddressLookupWebServiceImpl(
//      responseOfPostcodeWebService = FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddress,
//      responseOfUprnWebService = FakeAddressLookupWebServiceImpl.responseValidForUprnToAddress
//    )
//    bind[AddressLookupWebService].toInstance(fakeWebServiceImpl)
//  }
//
//}