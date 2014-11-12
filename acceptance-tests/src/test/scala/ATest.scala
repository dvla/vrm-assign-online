import cucumber.api.junit.Cucumber
import cucumber.api.junit.Cucumber.Options
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@Options(
  features = Array("acceptance-tests/src/test/resources/PersonalizedAssignment/"),
  glue = Array("PersonalizedAssignment"),
  tags = Array("@HappyPath,@UnHappyPath-InProgress,@UnHappyPath")
)
class RunTest {
}

object FeaturePath {
  final val Path = getClass.getClassLoader.getResource("/PersonalizedAssignment/").toURI.getPath
}

