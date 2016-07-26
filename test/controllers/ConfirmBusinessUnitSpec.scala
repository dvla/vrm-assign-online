package controllers

import composition.webserviceclients.audit2.AuditServiceDoesNothing
import helpers.UnitSpec
import helpers.vrm_assign.CookieFactoryForUnitSpecs.businessDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.setupBusinessDetails
import helpers.vrm_assign.CookieFactoryForUnitSpecs.storeBusinessDetailsConsent
import helpers.vrm_assign.CookieFactoryForUnitSpecs.trackingIdModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import helpers.TestWithApplication
import org.mockito.Mockito.verify
import pages.vrm_assign.SetupBusinessDetailsPage
import pages.vrm_assign.LeaveFeedbackPage
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.OK
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import scala.concurrent.duration.DurationInt
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.testhelpers.CookieHelper.fetchCookiesFromHeaders
import views.vrm_assign.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_assign.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import views.vrm_assign.VehicleLookup.UserType_Business
import webserviceclients.audit2.AuditRequest
import webserviceclients.fakes.AddressLookupServiceConstants.BusinessAddressLine1Valid
import webserviceclients.fakes.AddressLookupServiceConstants.BusinessAddressLine2Valid
import webserviceclients.fakes.AddressLookupServiceConstants.BusinessAddressPostTownValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.BusinessConsentValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.ReferenceNumberValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid

class ConfirmBusinessUnitSpec extends UnitSpec {

  "present" should {
    "display the page when required cookies are cached" in new TestWithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "redirect to setupBusinessDetails page when required cookies do not exist" in new TestWithApplication {
      val request = FakeRequest()
      val result = confirmBusiness.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "display a summary of previously entered user data" in new TestWithApplication {
      val content = contentAsString(present)
      content should include(BusinessAddressLine1Valid)
      content should include(BusinessAddressLine2Valid)
      content should include(BusinessAddressPostTownValid)
      content should include(RegistrationNumberValid)
    }
  }

  "submit" should {
    "call the audit service" in new TestWithApplication {
      val auditService2 = new AuditServiceDoesNothing
      val injector = testInjector(
        auditService2
      )

      val confirmBusiness = injector.getInstance(classOf[ConfirmBusiness])
      val dateService = injector.getInstance(classOf[DateService])

      val data = Seq(("transactionId", "ABC123123123123"),
        ("timestamp", dateService.dateTimeISOChronology),
        ("documentReferenceNumber", ReferenceNumberValid),
        ("currentVrm", "AB12AWR"),
        ("make", "Alfa Romeo"),
        ("model", "Alfasud ti"),
        ("keeperName", "MR DAVID JONES"),
        ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA"),
        ("businessName", "example trader contact"),
        ("businessAddress", "example trader name, " +
          "business line1 stub, business line2 stub, business postTown stub, QQ99QQ"),
        ("businessEmail", "business.example@test.com"))
      val auditRequest = new AuditRequest(AuditRequest.ConfirmBusinessToCaptureCertificateDetails,
        AuditRequest.AuditServiceType,
        data
      )
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          transactionId(),
          trackingIdModel()
        )
      val result = confirmBusiness.submit(request)
      whenReady(result) { r =>
        verify(auditService2.stub).send(auditRequest, TrackingId("trackingId"))
      }
    }

    "refresh all of the business details cookies to have a maxAge that is " +
      "7 days in the future if user is a business" in new TestWithApplication {
      val expected = 7.days.toSeconds.toInt
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          transactionId(),
          businessDetailsModel(),
          setupBusinessDetails(),
          storeBusinessDetailsConsent()
        )

      val result = confirmBusiness.submit(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(
          BusinessDetailsCacheKey,
          SetupBusinessDetailsCacheKey
          )
        cookies.find(_.name == BusinessDetailsCacheKey).get.maxAge.get === expected +- 1
        cookies.find(_.name == SetupBusinessDetailsCacheKey).get.maxAge.get === expected +- 1
      }
    }

    "refresh all of the business details cookies to have a maxAge that is " +
      "7 days in the future if user is a business and entered address manually" in new TestWithApplication {
      val expected = 7.days.toSeconds.toInt
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          transactionId(),
          businessDetailsModel(),
          setupBusinessDetails(),
          storeBusinessDetailsConsent()
        )

      val result = confirmBusiness.submit(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(
          BusinessDetailsCacheKey,
          SetupBusinessDetailsCacheKey
          )
        cookies.find(_.name == BusinessDetailsCacheKey).get.maxAge.get === expected +- 1
        cookies.find(_.name == SetupBusinessDetailsCacheKey).get.maxAge.get === expected +- 1
      }
    }
  }

  "back" should {
    "redirect to SetupBusinessDetails page when navigating back" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel()
        )
      val result = confirmBusiness.back(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }
  }

  "exit" should {
    "redirect to mock feedback page" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          transactionId()
        )
      val result = confirmBusiness.exit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(LeaveFeedbackPage.address))
      }
    }
  }

  private def confirmBusiness = testInjector().getInstance(classOf[ConfirmBusiness])

  private def present = {
    val request = FakeRequest()
      .withCookies(
        vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
        vehicleAndKeeperDetailsModel(),
        businessDetailsModel(),
        setupBusinessDetails()
      )
    confirmBusiness.present(request)
  }
}
