package controllers

import composition.TestAssignEmailServiceBinding
import composition.webserviceclients.paymentsolve.ValidatedAuthorised
import helpers.UnitSpec
import helpers.vrm_assign.CookieFactoryForUnitSpecs.businessDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.captureCertificateDetailsFormModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.captureCertificateDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.confirmFormModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.fulfilModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.paymentModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.paymentTransNo
import helpers.vrm_assign.CookieFactoryForUnitSpecs.setupBusinessDetails
import helpers.vrm_assign.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import helpers.TestWithApplication
import models.BusinessDetailsModel
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.ConfirmFormModel
import models.VehicleAndKeeperLookupFormModel
import org.mockito.{Mockito, Matchers}
import org.mockito.Matchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.scalatest.mock.MockitoSugar
import pages.vrm_assign.SuccessPage
import play.api.test.FakeRequest
import play.api.test.Helpers.BAD_REQUEST
import play.api.test.Helpers.CONTENT_DISPOSITION
import play.api.test.Helpers.CONTENT_TYPE
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.OK
import play.api.test.Helpers.status
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import webserviceclients.fakes.AddressLookupServiceConstants.KeeperEmailValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.BusinessConsentValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.ReplacementVRN

class SuccessUnitSpec extends UnitSpec with MockitoSugar {

  "present" should {
    "display the page when BusinessDetailsModel cookie exists" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())

      val (success, _) = build
      val result = success.present(request)
      status(result) should equal(OK)
    }

    "display the page when BusinessDetailsModel cookie does not exists" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          confirmFormModel(),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())

      val (success, _) = build
      val result = success.present(request)
      status(result) should equal(OK)
    }
  }

  "create pdf" should {
    "return a bad request if cookie for EligibilityModel does no exist" in new TestWithApplication {
      val request = FakeRequest().withCookies(transactionId())
      val (success, _) = build
      val result = success.createPdf(request)
      status(result) should equal(BAD_REQUEST)
    }

    "return a bad request if cookie for TransactionId does no exist" in new TestWithApplication {
      val request = FakeRequest().withCookies(fulfilModel())
      val (success, _) = build
      val result = success.createPdf(request)
      status(result) should equal(BAD_REQUEST)
    }

    "return a pdf when the cookie exists" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          vehicleAndKeeperDetailsModel(),
          captureCertificateDetailsFormModel(),
          captureCertificateDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          fulfilModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (success, _) = build
      val result = success.createPdf(request)
      whenReady(result) { r =>
        r.header.status should equal(OK)
        r.header.headers.get(CONTENT_DISPOSITION) should equal(Some(s"attachment;filename=$ReplacementVRN-eV948.pdf"))
        r.header.headers.get(CONTENT_TYPE) should equal(Some("application/pdf"))
      }
    }
  }

  private def build = {
    val assignEmailService = new TestAssignEmailServiceBinding
    val injector = testInjector(
      new ValidatedAuthorised(),
      assignEmailService
    )
    (injector.getInstance(classOf[Success]), assignEmailService.stub)
  }
}
