package PersonalizedAssignment

import cucumber.api.junit.Cucumber
import cucumber.api.junit.Cucumber.Options
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@Options(
  features = Array("acceptance-tests/src/test/resources/PersonalizedAssignment"),
  glue = Array("PersonalizedAssignment"),
  format = Array("pretty", "html:target/cucumber-report"),
  tags = Array("@HappyPath")
)
class RunCucumber {
}
