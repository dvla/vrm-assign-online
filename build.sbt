import de.johoop.jacoco4sbt.JacocoPlugin._
import net.litola.SassPlugin
import org.scalastyle.sbt.ScalastylePlugin
import Common._

name := "vrm-assign-online"

version := versionString

organization := organisationString

organizationName := organisationNameString

scalaVersion := scalaVersionString

scalacOptions := scalaOptionsSeq

publishTo.<<=(publishResolver)

credentials += sbtCredentials

resolvers ++= projectResolvers

lazy val root = (project in file(".")).enablePlugins(PlayScala, SassPlugin, SbtWeb)

lazy val acceptanceTestsProject = Project("acceptance-tests", file("acceptance-tests"))
  .dependsOn(root % "test->test")
  .disablePlugins(PlayScala, SassPlugin, SbtWeb)
  .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings:_*)

libraryDependencies ++= {
  Seq(
    cache,
    filters,
    "org.seleniumhq.selenium" % "selenium-java" % "2.44.0" % "test" withSources() withJavadoc(),
    "com.github.detro" % "phantomjsdriver" % "1.2.0" % "test" withSources() withJavadoc(),
    "info.cukes" % "cucumber-java" % "1.2.0" % "test" withSources() withJavadoc(),
    "org.specs2" %% "specs2" % "2.4" % "test" withSources() withJavadoc(),
    "org.mockito" % "mockito-all" % "1.9.5" % "test" withSources() withJavadoc(),
    "com.github.tomakehurst" % "wiremock" % "1.46" % "test" withSources() withJavadoc() exclude("log4j", "log4j"),
    "org.slf4j" % "log4j-over-slf4j" % "1.7.7" % "test" withSources() withJavadoc(),
    "org.scalatest" %% "scalatest" % "2.2.2" % "test" withSources() withJavadoc(),
    "com.google.inject" % "guice" % "4.0-beta5" withSources() withJavadoc(),
    "com.google.guava" % "guava" % "15.0" withSources() withJavadoc(), // See: http://stackoverflow.com/questions/16614794/illegalstateexception-impossible-to-get-artifacts-when-data-has-not-been-loaded
    "com.tzavellas" % "sse-guice" % "0.7.1" withSources() withJavadoc(), // Scala DSL for Guice
    "commons-codec" % "commons-codec" % "1.8" withSources() withJavadoc(),
    "org.apache.httpcomponents" % "httpclient" % "4.3.5" withSources() withJavadoc(),
    "org.apache.pdfbox" % "pdfbox" % "1.8.6" withSources() withJavadoc(),
    "org.apache.pdfbox" % "preflight" % "1.8.6" withSources() withJavadoc(),
    "com.sun.mail" % "javax.mail" % "1.5.2",
    "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.0",
    "dvla" %% "vehicles-presentation-common" % "2.10-SNAPSHOT" withSources() withJavadoc() exclude("junit", "junit-dep"),
    "dvla" %% "vehicles-presentation-common" % "2.10-SNAPSHOT" % "test" classifier "tests"  withSources() withJavadoc() exclude("junit", "junit-dep"),
    "uk.gov.dvla.iep" % "iep-messaging" % "2.0.0",
    "org.webjars" % "requirejs" % "2.1.14-1",
    // Auditing service
    "com.rabbitmq" % "amqp-client" % "3.4.1",
    "junit" % "junit" % "4.11",
    "junit" % "junit-dep" % "4.11"
  )
}

val myTestOptions =
  if (System.getProperty("include") != null) {
    Seq(testOptions in Test += Tests.Argument("include", System.getProperty("include")))
  } else if (System.getProperty("exclude") != null) {
    Seq(testOptions in Test += Tests.Argument("exclude", System.getProperty("exclude")))
  } else Seq.empty[Def.Setting[_]]

myTestOptions

// If tests are annotated with @LiveTest then they are excluded when running sbt test
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-l", "helpers.tags.LiveTest")

javaOptions in Test += System.getProperty("waitSeconds")

concurrentRestrictions in Global := Seq(Tags.limit(Tags.CPU, 4), Tags.limit(Tags.Network, 10), Tags.limit(Tags.Test, 4))

sbt.Keys.fork in Test := false

jacoco.settings

parallelExecution in jacoco.Config := false

// Using node to do the javascript optimisation cuts the time down dramatically
JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

// Disable documentation generation to save time for the CI build process
sources in doc in Compile := List()

ScalastylePlugin.Settings

net.virtualvoid.sbt.graph.Plugin.graphSettings

credentials += Credentials(Path.userHome / ".sbt/.credentials")

ScoverageSbtPlugin.instrumentSettings

ScoverageSbtPlugin.ScoverageKeys.excludedPackages in ScoverageSbtPlugin.scoverage := "<empty>;Reverse.*"

CoverallsPlugin.coverallsSettings

resolvers ++= projectResolvers

runMicroServicesTask

sandboxTask

runAsyncTask

testGatlingTask

sandboxAsyncTask

gatlingTask

resolvers ++= projectResolvers

lazy val p1 = osAddressLookup.disablePlugins(PlayScala, SassPlugin, SbtWeb)
lazy val p3 = vehicleAndKeeperLookup.disablePlugins(PlayScala, SassPlugin, SbtWeb)
lazy val p4 = vrmAssignEligibility.disablePlugins(PlayScala, SassPlugin, SbtWeb)
lazy val p5 = vrmAssignFulfil.disablePlugins(PlayScala, SassPlugin, SbtWeb)
lazy val p6 = legacyStubs.disablePlugins(PlayScala, SassPlugin, SbtWeb)
lazy val p7 = gatlingTests.disablePlugins(PlayScala, SassPlugin, SbtWeb)
val p8 = paymentSolve.disablePlugins(PlayScala, SassPlugin, SbtWeb)