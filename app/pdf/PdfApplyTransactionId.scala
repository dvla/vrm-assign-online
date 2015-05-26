package pdf

import java.io.{ByteArrayOutputStream, ByteArrayInputStream}

import org.apache.pdfbox.Overlay
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.{PDType1Font, PDFont}
import org.apache.pdfbox.pdmodel.{PDPage, PDDocument}
import play.api.Logger

class PdfApplyTransactionId {
  def apply(template: Array[Byte], transactionId: String): Array[Byte] = {
    implicit val document = new PDDocument()
    val page = new PDPage()
    implicit var contentStream: PDPageContentStream = new PDPageContentStream(document, page)
    try {
      contentStream.beginText()
      val size = 18
      val font = fontHelveticaBold(size = 18)
      contentStream.moveTextPositionByAmount(340, 390)
      contentStream.moveTextPositionByAmount((200 - width(font, transactionId, fontSize = size)) / 2, 0) // Centre the text.
      contentStream.drawString(transactionId) // Transaction ID
      contentStream.endText()
    } catch {
      case e: Exception => Logger.error(
        s"PdfApplyTransactionId error when writing transaction id ${e.getMessage} ${e.getStackTraceString}"
      )
    } finally contentStream.close()

    document.addPage(page)

    val page1 = new PDPage()
    new PDPageContentStream(document, page1).close()
    document.addPage(page1)

    val blankDoc = PDDocument.load(new ByteArrayInputStream(template))
    val overlay = new Overlay()
    val result = new ByteArrayOutputStream()
    overlay.overlay(document, blankDoc).save(result)
    result.toByteArray
  }

  private def fontHelveticaBold(size: Int)(implicit contentStream: PDPageContentStream): PDFont = {
    // Create a new font object selecting one of the PDF base fonts
    val font: PDFont = PDType1Font.HELVETICA_BOLD
    contentStream.setFont(font, size)
    font
  }

  // Return the width of a bounding box that surrounds the string.
  private def width(font: PDFont, content: String, fontSize: Int) = font.getStringWidth(content) / 1000 * fontSize
}
