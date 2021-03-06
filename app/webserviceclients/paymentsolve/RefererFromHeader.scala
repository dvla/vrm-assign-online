package webserviceclients.paymentsolve

import play.api.mvc.Request

trait RefererFromHeader {

  def fetch(implicit request: Request[_]): Option[String]

  def paymentCallbackUrl(referer: String, tokenBase64URLSafe: String): String
}
