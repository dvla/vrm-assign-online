package pages

import org.scalatest.selenium.WebBrowser.{click, currentUrl, pageSource, singleSel, submit}
import pages.vrm_assign.PaymentPage
import pages.vrm_assign.PaymentPage.expiryMonth
import pages.vrm_assign.PaymentPage.expiryYear
import pages.vrm_assign.PaymentPage.noJavaScriptContinueButton
import pages.vrm_assign.PaymentPage.payNow
import pages.vrm_assign.PaymentPage.submitButton
import pages.vrm_assign.PaymentPage.url
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class PaymentPageSteps(implicit webDriver: WebBrowserDriver)
  extends helpers.AcceptanceTestHelper {

  def `happy path` =
    `is displayed`
      .enter(cardholderName = "test", cardNumber = "4444333322221111", cardSecurityCode = "123")
      .`expiryDate`
      .`paynow`
      .`no javascript continue`
      .`enter password`
      .`no javascript submit`
      .`no javascript continue`

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
    this
  }

  def enter(cardholderName: String, cardNumber: String, cardSecurityCode: String) = {
    PaymentPage.cardholderName.value = cardholderName
    PaymentPage.cardNumber.value = cardNumber
    PaymentPage.cardSecurityCode.value = cardSecurityCode
    this
  }

  def `paynow` = {
    click on payNow
    this
  }

  def `expiryDate` = {
    singleSel(expiryMonth()).value = "08"
    singleSel(expiryYear()).value = org.joda.time.LocalDate.now.plusYears(3).getYear.toString
    this
  }

  def `enter password` = {
    eventually {
      pageSource should include("Please enter your password")
      PaymentPage.acsPassword.value = "password"
      submit()
    }
    this
  }

  def `no javascript continue` = {
    eventually {
      pageSource should include("please click the Continue button below.")
      noJavaScriptContinueButton.underlying.submit()
    }
    this
  }

  def `no javascript submit` = {
    eventually {
      pageSource should include("please click the Submit button below.")
      submitButton.underlying.submit()
    }
    this
  }
}
