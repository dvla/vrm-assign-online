package personalizedAssignment

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("acceptance-tests/src/test/resources/personalizedAssignment"),
  glue = Array("personalizedAssignment.stepDefs"),
  //  plugin = Array("pretty", "html:target/cucumber-report"),
  tags = Array("@HappyPath")
)
class RunCucumber