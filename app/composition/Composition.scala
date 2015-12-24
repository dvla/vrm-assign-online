package composition

import com.google.inject.Guice
import composition.webserviceclients.addresslookup.AddressLookupServiceBinding
import composition.webserviceclients.addresslookup.AddressLookupWebServiceBinding
import composition.webserviceclients.audit2
import composition.webserviceclients.audit2.AuditMicroServiceBinding
import composition.webserviceclients.bruteforceprevention.BruteForcePreventionServiceBinding
import composition.webserviceclients.bruteforceprevention.BruteForcePreventionWebServiceBinding
import composition.webserviceclients.emailservice.EmailServiceBinding
import composition.webserviceclients.emailservice.EmailServiceWebServiceBinding
import composition.webserviceclients.paymentsolve.PaymentServiceBinding
import composition.webserviceclients.paymentsolve.PaymentWebServiceBinding
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupServiceBinding
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupWebServiceBinding
import composition.webserviceclients.vrmassigneligibility.VrmAssignEligibilityServiceBinding
import composition.webserviceclients.vrmassigneligibility.VrmAssignEligibilityWebServiceBinding
import composition.webserviceclients.vrmassignfulfil.VrmAssignFulfilServiceBinding
import composition.webserviceclients.vrmassignfulfil.VrmAssignFulfilWebServiceBinding
import filters.ServiceOpenFilter
import play.filters.gzip.GzipFilter
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionFilter
import uk.gov.dvla.vehicles.presentation.common.filters.EnsureSessionCreatedFilter
import utils.helpers.ErrorStrategy

trait Composition {

  lazy val injector = Guice.createInjector(
    new DevModule,
    new AuditMicroServiceBinding,
    new audit2.AuditServiceBinding,
    new ConfigBinding,
    new AddressLookupServiceBinding,
    new AddressLookupWebServiceBinding,
    new VehicleAndKeeperLookupWebServiceBinding,
    new VehicleAndKeeperLookupServiceBinding,
    new DateServiceBinding,
    new CookieFlagsBinding,
    new VrmAssignEligibilityWebServiceBinding,
    new VrmAssignEligibilityServiceBinding,
    new VrmAssignFulfilWebServiceBinding,
    new VrmAssignFulfilServiceBinding,
    new PaymentWebServiceBinding,
    new PaymentServiceBinding,
    new SessionFactoryBinding,
    new BruteForcePreventionWebServiceBinding,
    new BruteForcePreventionServiceBinding,
    new LoggerLikeBinding,
    new PdfServiceBinding,
    new AssignEmailServiceBinding,
    new EmailServiceBinding,
    new EmailServiceWebServiceBinding,
    new RefererFromHeaderBinding,
    new DateTimeZoneServiceBinding,
    new HealthStatsBinding
  )

  lazy val filters = Array(
    injector.getInstance(classOf[EnsureSessionCreatedFilter]),
    new GzipFilter(),
    injector.getInstance(classOf[AccessLoggingFilter]),
    injector.getInstance(classOf[CsrfPreventionFilter]),
    injector.getInstance(classOf[ServiceOpenFilter])
  )

  lazy val errorStrategy = injector.getInstance(classOf[ErrorStrategy])
}