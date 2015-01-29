package composition

import com.google.inject.Guice
import composition.webserviceclients.addresslookup.{AddressLookupWebServiceBinding, AddressLookupServiceBinding}
import composition.webserviceclients.audit2.AuditServiceBinding
import play.filters.gzip.GzipFilter
import uk.gov.dvla.vehicles.presentation.common.filters.{AccessLoggingFilter, CsrfPreventionFilter, EnsureSessionCreatedFilter}
import utils.helpers.ErrorStrategy

trait Composition {

  lazy val injector = Guice.createInjector(
    new DevModule,
    new AuditServiceBinding,
    new ConfigBinding,
    new AddressLookupServiceBinding,
    new AddressLookupWebServiceBinding
  )

  lazy val filters = Array(
    injector.getInstance(classOf[EnsureSessionCreatedFilter]),
    new GzipFilter(),
    injector.getInstance(classOf[AccessLoggingFilter]),
    injector.getInstance(classOf[CsrfPreventionFilter])
  )

  lazy val errorStrategy = injector.getInstance(classOf[ErrorStrategy])
}