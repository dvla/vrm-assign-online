package personalizedAssignment

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("acceptance-tests/src/test/resources/personalizedAssignment"),
  //  features = Array("acceptance-tests/src/test/resources/personalizedAssignment/browser"),
  glue = Array("personalizedAssignment.stepDefs"),
  //  plugin = Array("pretty", "html:target/cucumber-report"),
  tags = Array("~@WIP", "~@browser", "~@live-payment")
  //  tags = Array("~@WIP", "~@live-payment")
  //  tags = Array("~@browser", "~@live-payment")
)
class RunCucumber