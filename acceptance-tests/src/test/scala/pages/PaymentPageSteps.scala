package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser._
import org.scalatest.selenium.WebBrowser.singleSel
import pages.vrm_assign.PaymentPage
import pages.vrm_assign.PaymentPage.expiryMonth
import pages.vrm_assign.PaymentPage.expiryYear
import pages.vrm_assign.PaymentPage.payNow
import pages.vrm_assign.PaymentPage.address
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class PaymentPageSteps(implicit webDriver: WebBrowserDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `happy path` = {
    `is displayed`.
      enter(cardholderName = "test", cardNumber = "4444333322221111", cardSecurityCode = "123").
      `expiryDate`.
      `paynow`.
      `enter password`
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should include(address)
    }(timeout)
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

    //DO NOT REMOVE COMMENTED CODE
    //     maximize
    //     theLogicaGroupLogo
    //    printf("The URL" + pageTitle)
    //    maximize
    //    //theLogicaGroupLogo
    //    printf("The URL" + pageTitle)

    //    implicitlyWait(Span(2,Minutes))
    this
  }

  def `expiryDate` = {
    singleSel(expiryMonth()).value = "08"
    singleSel(expiryYear()).value = "2018"
    this
  }

  def `enter password` = {
    eventually {
      pageSource should include("Please enter your password")
      PaymentPage.acsPassword.value = "password"
      submit()
    }(timeout)
    this
  }
}
