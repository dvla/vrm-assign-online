package composition

import com.google.inject.Guice
import composition.webserviceclients.addresslookup.{AddressLookupServiceBinding, AddressLookupWebServiceBinding}
import composition.webserviceclients.audit2
import composition.webserviceclients.audit2.AuditMicroServiceBinding
import composition.webserviceclients.bruteforceprevention.{BruteForcePreventionServiceBinding, BruteForcePreventionWebServiceBinding}
import composition.webserviceclients.emailservice.{EmailServiceWebServiceBinding, EmailServiceBinding}
import composition.webserviceclients.paymentsolve.{PaymentServiceBinding, PaymentWebServiceBinding}
import composition.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperLookupServiceBinding, VehicleAndKeeperLookupWebServiceBinding}
import composition.webserviceclients.vrmassigneligibility.{VrmAssignEligibilityServiceBinding, VrmAssignEligibilityWebServiceBinding}
import composition.webserviceclients.vrmassignfulfil.{VrmAssignFulfilServiceBinding, VrmAssignFulfilWebServiceBinding}
import play.filters.gzip.GzipFilter
import uk.gov.dvla.vehicles.presentation.common.filters.{AccessLoggingFilter, CsrfPreventionFilter, EnsureSessionCreatedFilter}
import utils.helpers.ErrorStrategy
import filters.ServiceOpenFilter

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
    new DateTimeZoneServiceBinding
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