package PersonalizedRegistration.runner.browser

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("acceptance-tests/src/test/resources/personalizedAssignment/browser/Navigation.feature"),
  glue = Array("personalizedAssignment.stepDefs"),
  tags = Array("~@WIP", "~@browser", "~@live-payment")
)
class Navigation
