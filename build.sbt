import Common._
import de.johoop.jacoco4sbt.JacocoPlugin._
import io.gatling.sbt.GatlingPlugin
import io.gatling.sbt.GatlingPlugin.Gatling
import org.scalastyle.sbt.ScalastylePlugin
import uk.gov.dvla.vehicles.sandbox.ProjectDefinitions.emailService
import uk.gov.dvla.vehicles.sandbox.ProjectDefinitions.legacyStubs
import uk.gov.dvla.vehicles.sandbox.ProjectDefinitions.osAddressLookup
import uk.gov.dvla.vehicles.sandbox.ProjectDefinitions.paymentSolve
import uk.gov.dvla.vehicles.sandbox.ProjectDefinitions.vehicleAndKeeperLookup
import uk.gov.dvla.vehicles.sandbox.ProjectDefinitions.vrmAssignEligibility
import uk.gov.dvla.vehicles.sandbox.ProjectDefinitions.vrmAssignFulfil
import uk.gov.dvla.vehicles.sandbox.Sandbox
import uk.gov.dvla.vehicles.sandbox.SandboxSettings
import uk.gov.dvla.vehicles.sandbox.Tasks
import com.typesafe.sbt.rjs.Import.RjsKeys.webJarCdns
import scoverage.ScoverageSbtPlugin.ScoverageKeys

name := "vrm-assign-online"

version := versionString

organization := organisationString

organizationName := organisationNameString

scalaVersion := scalaVersionString

scalacOptions := scalaOptionsSeq

publishTo.<<=(publishResolver)

credentials += sbtCredentials

resolvers ++= projectResolvers

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

lazy val acceptanceTestsProject = Project("acceptance-tests", file("acceptance-tests"))
  .dependsOn(root % "test->test")
  .disablePlugins(PlayScala, SbtWeb)
  .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)

lazy val gatlingTestsProject = Project("gatling-tests", file("gatling-tests"))
  .disablePlugins(PlayScala, SbtWeb)
  .enablePlugins(GatlingPlugin)

pipelineStages := Seq(rjs, digest, gzip)

libraryDependencies ++= {
  Seq(
    cache,
    filters,
    "org.seleniumhq.selenium" % "selenium-java" % "2.43.0" % "test",
//    "com.github.detro" % "phantomjsdriver" % "1.2.0" % "test" withSources() withJavadoc(),
    "com.codeborne" % "phantomjsdriver" % "1.2.1" % "test" withSources() withJavadoc(),
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
    "dvla" %% "vehicles-presentation-common" % "2.23-SNAPSHOT" withSources() withJavadoc() exclude("junit", "junit-dep"),
    "dvla" %% "vehicles-presentation-common" % "2.23-SNAPSHOT" % "test" classifier "tests"  withSources() withJavadoc() exclude("junit", "junit-dep"),
    "org.webjars" % "webjars-play_2.10" % "2.3.0-3",
    "org.webjars" % "requirejs" % "2.1.16",
    "org.webjars" % "jquery" % "1.9.1",
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

//////////////////
// Scoverage
//
// Code coverage plugin

ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;"

ScoverageKeys.coverageMinimum := 60

ScoverageKeys.coverageFailOnMinimum := true

ScoverageKeys.coverageHighlighting := true

// End Scoverage
//////////////////

resolvers ++= projectResolvers

webJarCdns := Map()

// ====================== Sandbox Settings ==========================
lazy val osAddressLookupProject = osAddressLookup("0.15-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val vehicleAndKeeperLookupProject = vehicleAndKeeperLookup("0.13-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val paymentSolveProject = paymentSolve("0.13-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val vrmAssignEligibilityProject = vrmAssignEligibility("0.10-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val vrmAssignFulfilProject = vrmAssignFulfil("0.9-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val legacyStubsProject = legacyStubs("1.0-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val emailServiceProject = emailService("0.7-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)

SandboxSettings.portOffset := 21000

SandboxSettings.applicationContext := ""

SandboxSettings.webAppSecrets := "ui/dev/vrm-assign-online.conf.enc"

SandboxSettings.osAddressLookupProject := osAddressLookupProject

SandboxSettings.vehicleAndKeeperLookupProject := vehicleAndKeeperLookupProject

SandboxSettings.paymentSolveProject := paymentSolveProject

SandboxSettings.vrmAssignEligibilityProject := vrmAssignEligibilityProject

SandboxSettings.vrmAssignFulfilProject := vrmAssignFulfilProject

SandboxSettings.legacyStubsProject := legacyStubsProject

SandboxSettings.emailServiceProject := emailServiceProject

SandboxSettings.runAllMicroservices := {
  Tasks.runLegacyStubs.value
  Tasks.runEmailService.value
  Tasks.runOsAddressLookup.value
  Tasks.runVehicleAndKeeperLookup.value
  Tasks.runPaymentSolve.value
  Tasks.runVrmAssignEligibility.value
  Tasks.runVrmAssignFulfil.value
}

SandboxSettings.loadTests := (test in Gatling in gatlingTestsProject).value

SandboxSettings.acceptanceTests := (test in Test in acceptanceTestsProject).value

SandboxSettings.bruteForceEnabled := true

Sandbox.sandboxTask

Sandbox.sandboxAsyncTask

Sandbox.gatlingTask

Sandbox.acceptTask

Sandbox.cucumberTask

Sandbox.acceptRemoteTask
