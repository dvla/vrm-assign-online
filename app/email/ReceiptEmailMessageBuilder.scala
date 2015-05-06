package email

import java.text.SimpleDateFormat

import org.joda.time.{DateTimeZone, Instant}

/**
 * The email message builder class will create the contents of the message. override the buildHtml and buildText
 * with new html and text templates respectively.
 *
 */
object ReceiptEmailMessageBuilder {
  import uk.gov.dvla.vehicles.presentation.common.services.SEND.Contents

  case class BusinessDetails(name: String, contact: String, address: Seq[String])

  def buildWith(assignVrn: String, amountCharged: String, transactionId: String, maskedCard: String,
                business: Option[BusinessDetails]): Contents = {

    val now = Instant.now.toDateTime(DateTimeZone.forID("Europe/London"))
    val dateStr = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(now.toDate)

    Contents(
      buildHtml(assignVrn, amountCharged, transactionId,maskedCard, dateStr, business.map(buildBusinessHtml).getOrElse("")),
      buildText(assignVrn, amountCharged, transactionId,maskedCard, dateStr, business.map(buildBusinessPlain).getOrElse(""))
    )
  }

   private def buildBusinessHtml(business: BusinessDetails): String =
   s"""
      |<ul>
      |<li>Business Only</li>
      |<li>Business Name: ${business.name}</li>
      |<li>Business Contact: ${business.contact}</li>
      |<li>Business Address: ${ (for {
           addr <- business.address
         } yield s"<li>$addr</li>").mkString("<ul>", "", "</ul>")  }</li>
       |</ul>
     """.stripMargin

  private def buildBusinessPlain(business: BusinessDetails): String =
  s"""
     |Business Only
     |Business Name
   }: ${business.name}
     |Business Contact: ${business.contact}
     |Business Address: ${business.address}
   """.stripMargin

  private def buildHtml(assignVrn: String,
                        amountCharged: String,
                        transactionId: String,
                        maskedCard: String,
                        dateStr: String,
                        business: String): String =
    s"""
       |<html>
       |<head>
       |</head>
       |<style>
       |p {
       |  line-height: 200%;
       |}
       |li { list-style: none; padding: 0; margin:0;}
       |</style>
       |</head>
       |<body>
       |
       |<p><b>THIS IS AN AUTOMATED EMAIL - PLEASE DO NOT REPLY.</b></p>
       |<p>Payment received.</p>
       |
       |<ul>
       |<li>£$amountCharged DVLA Online Assignment of $assignVrn</li>
       |<li>Card Number: $maskedCard</li>
       |<li>Date:  $dateStr</li>
       |<li>Transaction Number:  $transactionId</li>
       |</ul>
       |
       |$business
       |
       |<p>DVLA, Swansea, West Glamorgan SA99 1DN</p>
      """.stripMargin

  private def buildText(assignVrn: String,
                        amountCharged: String,
                        transactionId: String,
                        maskedCard: String,
                        dateStr: String,
                        business: String): String =
    s"""
       |THIS IS AN AUTOMATED EMAIL - PLEASE DO NOT REPLY.
       |
       |
       |Payment received
       |
       |£$amountCharged DVLA Online Assignment of $assignVrn
       |Card Number: $maskedCard
       |Date:  $dateStr
       |Transaction Number:  $transactionId
       |
       |$business
       |
       |DVLA, Swansea, West Glamorgan SA99 1DN
       |
      """.stripMargin

}
