package filters

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.filters.DateTimeZoneService
import uk.gov.dvla.vehicles.presentation.common.filters.EnsureServiceOpenFilter
import utils.helpers.Config

class ServiceOpenFilter @Inject()(implicit config: Config,
                                  timeZone: DateTimeZoneService,
                                  dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService)
  extends EnsureServiceOpenFilter {

  protected lazy val opening = config.openingTimeMinOfDay
  protected lazy val closing = config.closingTimeMinOfDay
  protected lazy val dateTimeZone = timeZone
  protected lazy val html = views.html.vrm_assign.closed("", "")
  override protected val closedDays = config.closedDays

  override protected def html(opening: String, closing: String) = views.html.vrm_assign.closed(opening, closing)
}