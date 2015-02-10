package webserviceclients.fakes

import play.api.http.Status.OK
import webserviceclients.emailservice.EmailServiceSendResponse

object EmailServiceWebServiceConstants {

  def emailServiceSendResponseSuccess: (Int, Option[EmailServiceSendResponse]) = {
    (OK, Some(new EmailServiceSendResponse()))
  }

}