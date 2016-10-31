package controllers

import com.tzavellas.sse.guice.ScalaModule
import composition.RefererFromHeaderBinding
import composition.webserviceclients.paymentsolve.TestPaymentWebServiceBinding.loadBalancerUrl
import composition.webserviceclients.paymentsolve.ValidatedCardDetails
import composition.webserviceclients.vrmassignfulfil.TestVrmAssignFulfilWebServiceBinding
import composition.webserviceclients.vrmassignfulfil.VrmAssignFulfilFailure
import controllers.Payment.AuthorisedStatus
import email.{AssignEmailServiceImpl, AssignEmailService}
import helpers.UnitSpec
import helpers.vrm_assign.CookieFactoryForUnitSpecs.businessDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.captureCertificateDetailsFormModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.captureCertificateDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.confirmFormModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.granteeConsent
import helpers.vrm_assign.CookieFactoryForUnitSpecs.paymentModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.paymentTransNo
import helpers.vrm_assign.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import helpers.TestWithApplication
import org.joda.time.DateTime
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import pages.vrm_assign.SuccessPage
import pdf.PdfService
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeHeaders
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, REFERER}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailService
import utils.helpers.Config
import webserviceclients.fakes.AddressLookupServiceConstants.KeeperEmailValid
import webserviceclients.fakes.CaptureCertificateDetailsWebServiceConstants.ExpiredWithFeeCertificate
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.{BusinessConsentValid, KeeperConsentValid}
import webserviceclients.vrmassignfulfil.VrmAssignFulfilRequest
import webserviceclients.vrmassignfulfil.VrmAssignFulfilWebService

class FulfilUnitSpec extends UnitSpec {

  val keeperEmail = "keeper.example@test.com"
  val businessEmail = "business.example@test.com"

  "fulfil" should {
    "redirect to error page when cookies do not exist" in new TestWithApplication {
      val request = FakeRequest()

      val result = fulfil.fulfil(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should
          equal(Some("/error/user%20went%20to%20fulfil%20mark%20without%20correct%20cookies"))
      }
    }

    "redirect to success page when no fees due and required cookies are present" in new TestWithApplication {
      val result = fulfil.fulfil(requestWithFeesNotDue())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))
      }
    }

    "redirect to success page when fees due and required cookies are present" in new TestWithApplication {
      val result = fulfil.fulfil(requestWithFeesDue())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))
      }
    }

    "redirect to error page when there are fees due but the payment status is not AUTHORISED" in new TestWithApplication {
      val result = fulfil.fulfil(requestWithFeesDue(paymentStatus = None))
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should
          equal(Some("/error/user%20went%20to%20fulfil%20mark%20without%20correct%20cookies"))
      }
    }

    "redirect to assign failure with a reg number that cannot be assigned" in new TestWithApplication {
      val (fulfilController, _) = fulfilControllerAndWebServiceMock((new VrmAssignFulfilFailure).stub)
      val result = fulfilController.fulfil(requestWithUnassignableRegNumber())

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some("/fulfil-failure"))
      }
    }

    "not send a payment email to the registered keeper and not to the business when " +
    "registered keeper is chosen and keeper email is not supplied" in new TestWithApplication {
      val (fulfilController, wsMock) = fulfilControllerAndWebServiceMock()
      val assignFulfilRequestArg = ArgumentCaptor.forClass(classOf[VrmAssignFulfilRequest])

      // user type: keeper
      // businessDetailsModel is populated
      // confirmModel created with no keeper email supplied
      val result = fulfilController.fulfil(requestWithFeesDue(keeperEmail = None))
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))

        verify(wsMock).invoke(assignFulfilRequestArg.capture(), any[TrackingId])

        val paymentSuccessReceiptEmails =
          assignFulfilRequestArg.getValue.paymentSolveUpdateRequest.get.businessReceiptEmails
        paymentSuccessReceiptEmails shouldBe empty

        val assignSuccessEmailRequests = assignFulfilRequestArg.getValue.successEmailRequests
        assignSuccessEmailRequests shouldBe empty
      }
    }

    "send a payment email to the registered keeper only and not to the business when " +
    "registered keeper is chosen and keeper email is supplied" in new TestWithApplication {
      val (fulfilController, wsMock) = fulfilControllerAndWebServiceMock()
      val assignFulfilRequestArg = ArgumentCaptor.forClass(classOf[VrmAssignFulfilRequest])

      // user type: keeper
      // businessDetailsModel is populated
      // confirmModel created with the keeper email supplied
      val result = fulfilController.fulfil(requestWithFeesDue())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))

        verify(wsMock).invoke(assignFulfilRequestArg.capture(), any[TrackingId])

        val paymentSuccessReceiptEmails =
          assignFulfilRequestArg.getValue.paymentSolveUpdateRequest.get.businessReceiptEmails
        paymentSuccessReceiptEmails.size should equal(1) // Based on user type = keeper
        paymentSuccessReceiptEmails.head.toReceivers should equal(Some(List(keeperEmail)))

        val assignSuccessEmailRequests = assignFulfilRequestArg.getValue.successEmailRequests
        // Email for the keeper because the keeper email is specified in confirmModel.
        // No business email because the user type is keeper
        assignSuccessEmailRequests.size should equal(1)

        val successEmail = assignSuccessEmailRequests.head
        successEmail.toReceivers should equal(Some(List(keeperEmail)))
        successEmail.attachment shouldBe defined
      }
    }

    "send a payment email to the business acting on behalf of the keeper and " +
      "not to the keeper when business is chosen and send retention success emails " +
      "to both business and keeper when keeper email is supplied" in new TestWithApplication {
      val (fulfilController, wsMock) = fulfilControllerAndWebServiceMock()
      val assignFulfilRequestArg = ArgumentCaptor.forClass(classOf[VrmAssignFulfilRequest])

      // user type: business
      // businessDetailsModel is populated
      // confirmModel created with the keeper email supplied
      val result = fulfilController.fulfil(requestWithFeesDue(keeperConsent = BusinessConsentValid))
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))

        verify(wsMock).invoke(assignFulfilRequestArg.capture(), any[TrackingId])

        val paymentSuccessReceiptEmails =
          assignFulfilRequestArg.getValue.paymentSolveUpdateRequest.get.businessReceiptEmails
        paymentSuccessReceiptEmails.size should equal(1) // Based on user type = business
        paymentSuccessReceiptEmails.head.toReceivers should equal(Some(List(businessEmail)))

        val retentionSuccessEmailRequests = assignFulfilRequestArg.getValue.successEmailRequests
        // 1 for business because user type = business and
        // 1 for keeper because the keeper email is supplied in the confirmModel
        retentionSuccessEmailRequests.size should equal(2)

        val businessSuccessEmail =  retentionSuccessEmailRequests.head
        val keeperSuccessEmail = retentionSuccessEmailRequests(1)

        businessSuccessEmail.toReceivers should equal(Some(List(businessEmail)))
        keeperSuccessEmail.toReceivers should equal(Some(List(keeperEmail)))

        businessSuccessEmail.attachment shouldBe defined
        keeperSuccessEmail.attachment shouldBe None
      }
    }

    "send a payment email and a retention success email to the business " +
      "acting on behalf of the keeper when business is chosen and no keeper email is supplied" in new TestWithApplication {
      val (fulfilController, wsMock) = fulfilControllerAndWebServiceMock()
      val assignFulfilRequestArg = ArgumentCaptor.forClass(classOf[VrmAssignFulfilRequest])

      // user type: business
      // businessDetailsModel is populated
      // confirmModel created with no keeper email supplied
      val result = fulfilController.fulfil(requestWithFeesDue(keeperConsent = BusinessConsentValid, keeperEmail = None))
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))

        verify(wsMock).invoke(assignFulfilRequestArg.capture(), any[TrackingId])

        val paymentSuccessReceiptEmails =
          assignFulfilRequestArg.getValue.paymentSolveUpdateRequest.get.businessReceiptEmails
        paymentSuccessReceiptEmails.size should equal(1)
        paymentSuccessReceiptEmails.head.toReceivers should equal(Some(List(businessEmail)))

        val retentionSuccessEmailRequests = assignFulfilRequestArg.getValue.successEmailRequests
        // Email for the business because the user type is business.
        // No keeper email because no keeper email supplied in ConfirmModel
        retentionSuccessEmailRequests.size should equal(1)

        val successEmail = retentionSuccessEmailRequests.head
        successEmail.toReceivers should equal(Some(List(businessEmail)))
        successEmail.attachment shouldBe defined
      }
    }
  }

  private def requestWithFeesNotDue(referer: String = loadBalancerUrl): FakeRequest[AnyContentAsEmpty.type] = {
    val refererHeader = (REFERER, Seq(referer))
    val headers = FakeHeaders(data = Seq(refererHeader))
    FakeRequest(method = "GET", uri = "/", headers = headers, body = AnyContentAsEmpty)
      .withCookies(
        vehicleAndKeeperLookupFormModel(),
        transactionId(),
        captureCertificateDetailsFormModel(),
        granteeConsent(),
        captureCertificateDetailsModel(),
        vehicleAndKeeperDetailsModel(),
        confirmFormModel()
      )
  }

  private def requestWithFeesDue(referer: String = loadBalancerUrl,
                                 paymentStatus: Option[String] = Some(AuthorisedStatus),
                                 keeperConsent: String = KeeperConsentValid,
                                 keeperEmail: Option[String] = KeeperEmailValid
                                ): FakeRequest[AnyContentAsEmpty.type] = {
    val refererHeader = (REFERER, Seq(referer))
    val headers = FakeHeaders(data = Seq(refererHeader))
    FakeRequest(method = "GET", uri = "/", headers = headers, body = AnyContentAsEmpty)
      .withCookies(
        vehicleAndKeeperLookupFormModel(registrationNumber = "DD22", keeperConsent = keeperConsent),
        businessDetailsModel(),
        transactionId(),
        captureCertificateDetailsFormModel(),
        granteeConsent(),
        captureCertificateDetailsModel(certificate = ExpiredWithFeeCertificate),
        paymentModel(paymentStatus = paymentStatus),
        paymentTransNo(),
        vehicleAndKeeperDetailsModel(registrationNumber = "DD22"),
        confirmFormModel(keeperEmail = keeperEmail)
      )
  }

  private def requestWithUnassignableRegNumber(referer: String = loadBalancerUrl): FakeRequest[AnyContentAsEmpty.type] = {
    val refererHeader = (REFERER, Seq(referer))
    val headers = FakeHeaders(data = Seq(refererHeader))
    FakeRequest(method = "GET", uri = "/", headers = headers, body = AnyContentAsEmpty)
      .withCookies(
        vehicleAndKeeperLookupFormModel(registrationNumber = "FF111"),
        transactionId(),
        captureCertificateDetailsFormModel(),
        granteeConsent(),
        captureCertificateDetailsModel(),
        vehicleAndKeeperDetailsModel(registrationNumber = "FF111"),
        confirmFormModel()
      )
  }

  private def fulfil = testInjector(
    new ValidatedCardDetails(),
    new RefererFromHeaderBinding
  ).getInstance(classOf[Fulfil])

  private def fulfilControllerAndWebServiceMock(assignWSMock: VrmAssignFulfilWebService =
                                                (new TestVrmAssignFulfilWebServiceBinding).stub)
                                                                              : (Fulfil, VrmAssignFulfilWebService) = {

    val fulfil = testInjector(
      new ValidatedCardDetails(),
      new RefererFromHeaderBinding,
      // Bind the mock to the trait. We have a reference to the mock which we can pass out of this method
      // so client code can perform expectations on it
      new ScalaModule() {
        override def configure(): Unit = bind[VrmAssignFulfilWebService].toInstance(assignWSMock)
      },
      // By default the testInjector mocks the RetainEmailService. However, we want a real instance of the
      // RetainEmailService because it contains logic that needs to be tested. So we bind a real instance
      // that has mocks for all it dependencies
      new ScalaModule() {
        override def configure(): Unit = bind[AssignEmailService].toInstance(assignEmailServiceInstance)
      }
    ).getInstance(classOf[Fulfil])
    (fulfil, assignWSMock)
  }

  private def assignEmailServiceInstance: AssignEmailService = {
    val emailServiceMock = mock[EmailService]

    val pdfServiceMock = mock[PdfService]
    when(pdfServiceMock.create(any[String], any[String], any[Option[AddressModel]], any[String], any[TrackingId]))
      .thenReturn(Array.ofDim[Byte](0))

    val configMock = mock[Config]
    when(configMock.emailWhitelist).thenReturn(Some(List("@test.com")))
    when(configMock.renewalFeeInPence).thenReturn("2500")

    new AssignEmailServiceImpl(emailServiceMock, pdfServiceMock, configMock)
  }
}
