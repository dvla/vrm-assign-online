package composition

import com.google.inject.Guice
import composition.webserviceclients.addresslookup.{AddressLookupServiceBinding, AddressLookupWebServiceBinding}
import composition.webserviceclients.audit2.AuditServiceBinding
import composition.webserviceclients.paymentsolve.{PaymentServiceBinding, PaymentWebServiceBinding}
import composition.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperLookupServiceBinding, VehicleAndKeeperLookupWebServiceBinding}
import composition.webserviceclients.vrmassigneligibility.{VrmAssignEligibilityServiceBinding, VrmAssignEligibilityWebServiceBinding}
import composition.webserviceclients.vrmassignfulfil.{VrmAssignFulfilServiceBinding, VrmAssignFulfilWebServiceBinding}
import play.filters.gzip.GzipFilter
import uk.gov.dvla.vehicles.presentation.common.filters.{AccessLoggingFilter, CsrfPreventionFilter, EnsureSessionCreatedFilter}
import utils.helpers.ErrorStrategy

trait Composition {

  lazy val injector = Guice.createInjector(
    new DevModule,
    new AuditServiceBinding,
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
    new PaymentServiceBinding
  )

  lazy val filters = Array(
    injector.getInstance(classOf[EnsureSessionCreatedFilter]),
    new GzipFilter(),
    injector.getInstance(classOf[AccessLoggingFilter]),
    injector.getInstance(classOf[CsrfPreventionFilter])
  )

  lazy val errorStrategy = injector.getInstance(classOf[ErrorStrategy])
}