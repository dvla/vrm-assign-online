package controllers

import composition.TestConfig
import controllers.Common.PrototypeHtml
import helpers.UnitSpec
import helpers.vrm_assign.CookieFactoryForUnitSpecs.{setupBusinessDetails, vehicleAndKeeperDetailsModel}
import helpers.TestWithApplication
import models.SetupBusinessDetailsFormModel
import pages.vrm_assign.{ConfirmBusinessPage, VehicleLookupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, LOCATION, OK, contentAsString, defaultAwaitTimeout}
import uk.gov.dvla.vehicles.presentation.common.mappings.{AddressPicker, BusinessName}
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.{EmailId, EmailVerifyId}
import uk.gov.dvla.vehicles.presentation.common.testhelpers.CookieHelper.fetchCookiesFromHeaders
import uk.gov.dvla.vehicles.presentation.common.testhelpers.JsonUtils.deserializeJsonToModel
import views.vrm_assign.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_assign.SetupBusinessDetails.BusinessAddressId
import views.vrm_assign.SetupBusinessDetails.BusinessContactId
import views.vrm_assign.SetupBusinessDetails.BusinessEmailId
import views.vrm_assign.SetupBusinessDetails.BusinessNameId
import views.vrm_assign.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import webserviceclients.fakes.AddressLookupServiceConstants.PostcodeValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessContactValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessEmailValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessNameValid

class SetUpBusinessDetailsUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new TestWithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "display populated fields when cookie exists" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          setupBusinessDetails(),
          vehicleAndKeeperDetailsModel()
        )
      val result = setUpBusinessDetails().present(request)
      val content = contentAsString(result)
      content should include(TraderBusinessNameValid)
      content should include(PostcodeValid)
    }

    "display empty fields when setupBusinessDetails cookie does not exist" in new TestWithApplication {
      val content = contentAsString(present)
      content should not include TraderBusinessNameValid
      content should not include PostcodeValid
    }

    "display prototype message when config set to true" in new TestWithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new TestWithApplication {
      val request = FakeRequest()
      val result = setUpBusinessDetailsPrototypeNotVisible().present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {
    "redirect to VehicleLookup page if required cookies do not exist" in new TestWithApplication {
      val request = FakeRequest()
        .withFormUrlEncodedBody(s"$BusinessAddressId.${AddressPicker.ShowSearchFields}" -> true.toString)
      val result = setUpBusinessDetails().submit(request)
      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to next page when the form is completed successfully" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = setUpBusinessDetails().submit(request)
      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(ConfirmBusinessPage.address))
          val cookies = fetchCookiesFromHeaders(r)
          val cookieName = SetupBusinessDetailsCacheKey
          cookies.find(_.name == cookieName) match {
            case Some(cookie) =>
              val json = cookie.value
              val model = deserializeJsonToModel[SetupBusinessDetailsFormModel](json)
              model.name should equal(TraderBusinessNameValid.toUpperCase)
              model.address.postCode should equal(PostcodeValid.toUpperCase)
            case None => fail(s"$cookieName cookie not found")
          }
      }
    }

    "return a bad request if no details are entered" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(dealerName = "", dealerPostcode = "")
        .withCookies(vehicleAndKeeperDetailsModel())
      val result = setUpBusinessDetails().submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "replace max length error message for traderBusinessName " +
      "with standard error message (US158)" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(dealerName = "a" * (BusinessName.MaxLength + 1))
        .withCookies(vehicleAndKeeperDetailsModel())
      val result = setUpBusinessDetails().submit(request)
      val content = contentAsString(result)
      val count = "Must be between 2 and 58 characters and only contain valid characters"
        .r.findAllIn(content).length
      count should equal(2) // The same message is displayed in 2 places - once in the validation-summary at the top of
      // the page and once above the field.
    }

    "replace required and min length error messages for traderBusinessName " +
      "with standard error message (US158)" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(dealerName = "")
        .withCookies(vehicleAndKeeperDetailsModel())
      val result = setUpBusinessDetails().submit(request)
      val content = contentAsString(result)
      val count = "Must be between 2 and 58 characters and only contain valid characters"
        .r.findAllIn(content).length
      count should equal(2) // The same message is displayed in 2 places - once in the validation-summary at the top of
      // the page and once above the field.
    }

    "write cookie when the form is completed successfully" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = setUpBusinessDetails().submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(SetupBusinessDetailsCacheKey)
      }
    }

    "write StoreBusinessDetails cookie with a maxAge 7 days in the future" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = setUpBusinessDetails().submit(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.filter(_.name == StoreBusinessDetailsCacheKey).map(_.value) should equal(Seq(true.toString))
      }
    }
  }

  private def setUpBusinessDetails() = testInjector().getInstance(classOf[SetUpBusinessDetails])

  private def present = {
    val request = FakeRequest().withCookies(vehicleAndKeeperDetailsModel())
    setUpBusinessDetails().present(request)
  }

  private def setUpBusinessDetailsPrototypeNotVisible() = {
    testInjector(new TestConfig(isPrototypeBannerVisible = false))
      .getInstance(classOf[SetUpBusinessDetails])
  }

  private def buildCorrectlyPopulatedRequest(dealerName: String = TraderBusinessNameValid,
                                             dealerContact: String = TraderBusinessContactValid,
                                             dealerEmail: String = TraderBusinessEmailValid,
                                             searchPostCode: String = "AA11AA",
                                             addressListSelect: String = "1",
                                             showSearchFields: Boolean = true,
                                             showAddressSelect: Boolean = true,
                                             showAddressFields: Boolean = true,
                                             addressLine1: String = "543 Great Nortfort St.",
                                             addressLine2: String = "Flat 12, Forest House,",
                                             addressLine3: String = "",
                                             postTown: String = "London",
                                             dealerPostcode: String = PostcodeValid,
                                             saveDetails: Boolean = true) = {
    val data = Seq(
      BusinessNameId -> dealerName,
      BusinessContactId -> dealerContact,
      s"$BusinessEmailId.$EmailId" -> dealerEmail,
      s"$BusinessEmailId.$EmailVerifyId" -> dealerEmail,
      s"$BusinessAddressId.${AddressPicker.SearchByPostcodeField}" -> searchPostCode,
      s"$BusinessAddressId.${AddressPicker.AddressListSelect}" -> addressListSelect,
      s"$BusinessAddressId.${AddressPicker.ShowSearchFields}" -> showSearchFields.toString,
      s"$BusinessAddressId.${AddressPicker.ShowAddressSelect}" -> showAddressSelect.toString,
      s"$BusinessAddressId.${AddressPicker.ShowAddressFields}" -> showAddressFields.toString,
      s"$BusinessAddressId.${AddressPicker.AddressLine1Id}" -> addressLine1,
      s"$BusinessAddressId.${AddressPicker.AddressLine2Id}" -> addressLine2,
      s"$BusinessAddressId.${AddressPicker.AddressLine3Id}" -> addressLine3,
      s"$BusinessAddressId.${AddressPicker.PostTownId}" -> postTown,
      s"$BusinessAddressId.${AddressPicker.PostcodeId}" -> dealerPostcode
    ) ++ (if (saveDetails) Seq(s"$BusinessAddressId.${AddressPicker.RememberId}" -> "true")
    else Seq.empty[(String, String)])
    FakeRequest().withFormUrlEncodedBody(data:_*)
  }
}
