package helpers.common

import composition.TestGlobal
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication

object ProgressBar {

  val fakeApplicationWithProgressBarFalse = LightFakeApplication(
    global = TestGlobal,
    additionalConfiguration = Map("progressBar.enabled" -> "false"))

  val fakeApplicationWithProgressBarTrue = LightFakeApplication(
    global = TestGlobal,
    additionalConfiguration = Map("progressBar.enabled" -> "true"))

  def progressStep(currentStep: Int): String = {
    val end = 6
    s"Step $currentStep of $end"
  }

  final val div: String = """<div class="progress-indicator">"""
}