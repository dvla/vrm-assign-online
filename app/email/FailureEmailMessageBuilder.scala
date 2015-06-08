package email

import java.text.SimpleDateFormat

import org.joda.time.{DateTimeZone, Instant}

/**
 * The email message builder class will create the contents of the message. override the buildHtml and buildText
 * with new html and text templates respectively.
 *
 */

// TODO : build the failure message, instead of the receipt as is does at the moment
// Works as a placeholder while we bolt everything together
// If this is exactly the same as the email for retain, we should move it  into common somewhere...

object FailureEmailMessageBuilder {
  import uk.gov.dvla.vehicles.presentation.common.services.SEND.Contents

  def buildWith: Contents = {

    val now = Instant.now.toDateTime(DateTimeZone.forID("Europe/London"))
    val dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(now.toDate)

    Contents(
      buildHtml,
      buildText
    )
  }

  private def buildHtml: String =
    s"""
       |<html>
       |<head>
       |</head>
       |<style>
       |p {
       |  line-height: 200%;
       |}
       |ul { list-style: none; padding: 0; margin:0 0 32px 0;}
       |li { margin-bottom: 8px}
       |li > ul {
       |	margin: 16px 0 0 16px;
       |}
       |</style>
       |</head>
       |<body>
       |
       |<p>
       |	<strong>THIS IS AN AUTOMATED EMAIL - PLEASE DO NOT REPLY.</strong>
       |</p>
       |
       |<p>Payment received.</p>
       |
        |<p><i>DVLA, Swansea, SA6 7JL</i></p>
      """.stripMargin

  private def buildText: String =
    s"""
       |THIS IS AN AUTOMATED EMAIL - PLEASE DO NOT REPLY.
       |
       |
        |
        |DVLA, Swansea, SA6 7JL
        |
      """.stripMargin

}
