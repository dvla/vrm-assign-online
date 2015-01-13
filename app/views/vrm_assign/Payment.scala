package views.vrm_assign

import models.CacheKeyPrefix

object Payment {

  final val GetWebPaymentlId = "get-web-payment"
  final val CancelId = "cancel"
  final val ExitId = "exit"
  final val PaymentDetailsCacheKey = s"${CacheKeyPrefix}payment-details"
  final val PaymentTransNoCacheKey = s"${CacheKeyPrefix}payment-trans-no"
}