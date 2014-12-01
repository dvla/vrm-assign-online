package controllers

import helpers.UnitSpec
import models.ConfirmFormModel
import play.api.data.Form
import views.vrm_assign.Confirm._
import webserviceclients.fakes.ConfirmFormConstants.KeeperEmailValid

final class ConfirmFormSpec extends UnitSpec {

  "form" should {

    "accept when all fields contain valid responses" in {
      formWithValidDefaults().get.keeperEmail.get should equal(KeeperEmailValid.toString)
    }
  }

  private def formWithValidDefaults(keeperEmail: String = KeeperEmailValid.toString) = {
    Form(ConfirmFormModel.Form.Mapping).bind(
      Map(KeeperEmailId -> keeperEmail)
    )
  }
}