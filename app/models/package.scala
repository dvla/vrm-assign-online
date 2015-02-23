package object models {

  implicit final val CacheKeyPrefix = uk.gov.dvla.vehicles.presentation.common.model.CacheKeyPrefix("asn-")
  final val PrScopedCacheKeyPrefix = "ret-asn-"
  final val SeenCookieMessageCacheKey = "seen_cookie_message"
}
