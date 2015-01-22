package composition

import com.google.inject.name.Names
import com.google.inject.util.Modules
import com.google.inject.{Guice, Injector, Module}
import com.tzavellas.sse.guice.ScalaModule
import composition.audit2.AuditServiceReal
import composition.paymentsolvewebservice.TestPaymentSolveWebService
import composition.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebService
import email.{EmailService, EmailServiceImpl}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import pdf.{PdfService, PdfServiceImpl}
import play.api.i18n.Lang
import play.api.{Logger, LoggerLike}
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession._
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter._
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.{AddressLookupServiceImpl, WebServiceImpl}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.{AddressLookupService, AddressLookupWebService}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.{BruteForcePreventionServiceImpl, BruteForcePreventionService, BruteForcePreventionWebService}
import utils.helpers.{AssignCookieFlags, Config}
import webserviceclients.fakes.AddressLookupServiceConstants._
import webserviceclients.fakes.AddressLookupWebServiceConstants
import webserviceclients.paymentsolve.{PaymentSolveServiceImpl, PaymentSolveService, PaymentSolveWebService}
import webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperLookupServiceImpl, VehicleAndKeeperLookupService, VehicleAndKeeperLookupWebService}
import webserviceclients.vrmassignfulfil.{VrmAssignFulfilService, VrmAssignFulfilServiceImpl, VrmAssignFulfilWebService, VrmAssignFulfilWebServiceImpl}
import webserviceclients.vrmretentioneligibility.{VrmAssignEligibilityService, VrmAssignEligibilityServiceImpl, VrmAssignEligibilityWebService, VrmAssignEligibilityWebServiceImpl}

trait TestComposition extends Composition {

  override lazy val injector: Injector = testInjector()

  def testInjector(modules: Module*) = {
    val overriddenDevModule = Modules.`override`(
      new TestModule(),
      new TestOrdnanceSurvey
    ).`with`(modules: _*)
    Guice.createInjector(overriddenDevModule)
  }
}

final class TestModule extends ScalaModule with MockitoSugar {

  def configure() {

    bind[Config].toInstance(new TestConfig().build)

    bind[VehicleAndKeeperLookupWebService].toInstance(new TestVehicleAndKeeperLookupWebService().build())
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
    bind[audit1.AuditService].toInstance(new composition.audit1Mock.MockAuditLocalService().build())
    bind[RefererFromHeader].toInstance(new TestRefererFromHeader().build)
    bind[webserviceclients.audit2.AuditMicroService].to[webserviceclients.audit2.AuditMicroServiceImpl].asEagerSingleton()
    bind[webserviceclients.audit2.AuditService].toInstance(new audit2.AuditServiceDoesNothing().build())
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