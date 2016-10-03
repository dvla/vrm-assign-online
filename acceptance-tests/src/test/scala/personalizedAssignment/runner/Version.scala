package PersonalizedAssignment.runner

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("acceptance-tests/src/test/resources/personalizedAssignment/Version.feature"),
  glue = Array("personalizedAssignment.stepDefs")
)
class Version
