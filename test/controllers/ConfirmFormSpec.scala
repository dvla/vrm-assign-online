package controllers

import helpers.UnitSpec
import models.ConfirmFormModel
import play.api.data.Form
import uk.gov.dvla.vehicles.presentation.common.mappings.OptionalToggle
import views.vrm_assign.Confirm.{GranteeConsentId, KeeperEmailId, SupplyEmailId}
import webserviceclients.fakes.ConfirmFormConstants.GranteeConsentValid
import webserviceclients.fakes.ConfirmFormConstants.KeeperEmailValid
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.{EmailId, EmailVerifyId}

class ConfirmFormSpec extends UnitSpec {

  "form" should {
    "accept when the keeper wants an email and does provide an email address" in {
      val model = buildForm().get

      model.granteeConsent should equal(GranteeConsentValid)
      model.keeperEmail should equal(Some(KeeperEmailValid))
    }

    "accept when the keeper does not want an email and does not provide an email address" in {
      val model = buildForm(keeperEmail = None).get

      model.granteeConsent should equal(GranteeConsentValid)
      model.keeperEmail should equal(None)
    }

    "reject when the keeper wants an email but does not enter an email address" in {
      val errors = buildForm(keeperEmail = Some(keeperEmailEmpty)).errors
      errors.length should equal(1)
      errors.head.key should equal(KeeperEmailId)
      errors.head.message should equal("error.email")
    }
  }

  private def buildForm(keeperEmail: Option[String] = Some(KeeperEmailValid),
                        granteeConsent: String = GranteeConsentValid.toString) = {
    Form(ConfirmFormModel.Form.Mapping).bind(
      Map(
      ) ++ keeperEmail.fold(Map(SupplyEmailId -> OptionalToggle.Invisible, GranteeConsentId -> granteeConsent)) { email =>
        Map(
          SupplyEmailId -> OptionalToggle.Visible,
          s"$KeeperEmailId.$EmailId" -> email,
          s"$KeeperEmailId.$EmailVerifyId" -> email,
          GranteeConsentId -> granteeConsent
        )
      }
    )
  }

  private val keeperEmailEmpty = ""
}