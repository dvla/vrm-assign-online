package PersonalizedAssignment.StepDefs

import cucumber.api.java.en.{Then, Given, When}
import cucumber.api.scala.{EN, ScalaDsl}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser._
import org.scalatest.Matchers

class VehiclePersonalAssignmentStepDefs (implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  @Given("^that I have started the PR Assign Service$")
  def `that_I_have_started_the_PR_Assign_Service`(){

  }

  @When("^I enter data in the \"(.*?)\",\"(.*?)\" and \"(.*?)\" for a vehicle that is eligible for retention$")
  def `i_enter_data_in_the_and_for_a_vehicle_that_is_eligible_for_retention`(arg1:String, arg2:String, arg3: String) {

  }
  @When("^I indicate that the keeper is acting$")
  def `i_indicate_that_the_keeper_is_acting`(){

  }

  @When("^enter \"(.*?)\",\"(.*?)\",\"(.*?)\",\"(.*?)\"  and \"(.*?)\"$")
  def `enter_and`(arg1:String,arg2:String ,arg3:String ,arg4:String ,arg5:String){

  }
  @Then("^the enter confirm details page is displayed and the payment required section is shown$")
  def `the_enter_confirm_details_page_is_displayed_and_the_payment_required_section_is_shown`(){

  }


}
