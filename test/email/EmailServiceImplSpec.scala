package email

import composition.AssignEmailServiceBinding

import helpers.UnitSpec
import helpers.TestWithApplication

import models.{VehicleAndKeeperLookupFormModel, BusinessDetailsModel, CaptureCertificateDetailsModel, CaptureCertificateDetailsFormModel, ConfirmFormModel}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import views.vrm_assign.VehicleLookup.{UserType_Business, UserType_Keeper}
import webserviceclients.fakes.AddressLookupServiceConstants.addressWithoutUprn
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessContactValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessEmailValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessNameValid
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateDateValid
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateDocumentCountValid
import webserviceclients.fakes.CaptureCertificateDetailsFormWebServiceConstants.CertificateTimeValid
import webserviceclients.fakes.CaptureCertificateDetailsWebServiceConstants.ValidCertificate
import webserviceclients.fakes.ConfirmFormConstants.GranteeConsentValid
import webserviceclients.fakes.ConfirmFormConstants.KeeperEmailValid
import webserviceclients.fakes.VrmAssignFulfilWebServiceConstants.TransactionTimestampValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperPostCodeValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.ReplacementVRN
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.ReferenceNumberValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.VehicleMakeValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.VehicleModelValid

final class EmailServiceImplSpec extends UnitSpec {

  "EmailRequest" should {
      "have an attachment if send pdf is true" in new TestWithApplication {
        val (dateService, emailService) = build

        val emailRequest = emailService.emailRequest(
          emailAddress = TraderBusinessEmailValid,
          vehicleAndKeeperDetailsModel = vehicleAndKeeperDetails,
          captureCertificateDetailsFormModel = captureCertificateDetailsFormModel,
          captureCertificateDetailsModel = captureCertificateDetailsModel,
          vehicleAndKeeperLookupFormModel = vehicleAndKeeperLookupFormModel(UserType_Business),
          transactionTimestamp = TransactionTimestampValid,
          transactionId = TransactionId,
          confirmFormModel = Some(confirmFormModel),
          businessDetailsModel = Some(businessDetailsModel),
          sendPdf = true,
          isKeeper = false,
          trackingId = TrackingId("123")
        )

        emailRequest shouldBe defined
        emailRequest.get.attachment shouldBe defined
      }

      "not have an attachment if send pdf is false" in new TestWithApplication {
        val (dateService, emailService) = build

        val emailRequest = emailService.emailRequest(
          emailAddress = TraderBusinessEmailValid,
          vehicleAndKeeperDetailsModel = vehicleAndKeeperDetails,
          captureCertificateDetailsFormModel = captureCertificateDetailsFormModel,
          captureCertificateDetailsModel = captureCertificateDetailsModel,
          vehicleAndKeeperLookupFormModel = vehicleAndKeeperLookupFormModel(UserType_Keeper),
          transactionTimestamp = TransactionTimestampValid,
          transactionId = TransactionId,
          confirmFormModel = Some(confirmFormModel),
          businessDetailsModel = None,
          sendPdf = false,
          isKeeper = true,
          trackingId = TrackingId("123")
        )

        emailRequest shouldBe defined
        emailRequest.get.attachment shouldBe empty
      }
    }

  "htmlMessage" should {
    "return html with business details when user type is business" in new TestWithApplication {
      val (_, emailService) = build

      val result = emailService.htmlMessage(
        vehicleAndKeeperDetailsModel = vehicleAndKeeperDetails,
        captureCertificateDetailsFormModel= captureCertificateDetailsFormModel,
        captureCertificateDetailsModel = captureCertificateDetailsModel,
        vehicleAndKeeperLookupFormModel = vehicleAndKeeperLookupFormModel(UserType_Business),
        transactionTimestamp = TransactionTimestampValid,
        transactionId = TransactionId,
        confirmFormModel = Some(confirmFormModel),
        businessDetailsModel = Some(businessDetailsModel),
        isKeeper = false
      )

      val message = result.toString

      val retentionCertId = captureCertificateDetailsFormModel.certificateDocumentCount + " " +
        captureCertificateDetailsFormModel.certificateDate + " " +
        captureCertificateDetailsFormModel.certificateTime + " " +
        captureCertificateDetailsFormModel.certificateRegistrationMark

      message should include(vehicleAndKeeperDetails.registrationNumber)
      message should include(captureCertificateDetailsModel.prVrm)
      message should include(retentionCertId)
      message should include(TransactionTimestampValid)
      message should include(TransactionId)
      message should include(TraderBusinessNameValid)
      message should include(TraderBusinessEmailValid)
    }

    "return html without business details html when user type is keeper" in new TestWithApplication {
      val (_, emailService) = build

      val result = emailService.htmlMessage(
        vehicleAndKeeperDetailsModel = vehicleAndKeeperDetails,
        captureCertificateDetailsFormModel= captureCertificateDetailsFormModel,
        captureCertificateDetailsModel = captureCertificateDetailsModel,
        vehicleAndKeeperLookupFormModel = vehicleAndKeeperLookupFormModel(UserType_Keeper),
        transactionTimestamp = TransactionTimestampValid,
        transactionId = TransactionId,
        confirmFormModel = Some(confirmFormModel),
        businessDetailsModel = None,
        isKeeper = true
      )

      val message = result.toString

      val retentionCertId = captureCertificateDetailsFormModel.certificateDocumentCount + " " +
        captureCertificateDetailsFormModel.certificateDate + " " +
        captureCertificateDetailsFormModel.certificateTime + " " +
        captureCertificateDetailsFormModel.certificateRegistrationMark

      message should include(vehicleAndKeeperDetails.registrationNumber)
      message should include(captureCertificateDetailsModel.prVrm)
      message should include(retentionCertId)
      message should include(TransactionTimestampValid)
      message should include(TransactionId)
      message shouldNot include(TraderBusinessNameValid)
      message shouldNot include(TraderBusinessEmailValid)
    }
  }

  private def build = {
    val injector = testInjector(
      new AssignEmailServiceBinding
    )
    (injector.getInstance(classOf[DateService]), injector.getInstance(classOf[AssignEmailService]))
  }

  private def vehicleAndKeeperDetails = VehicleAndKeeperDetailsModel(
    registrationNumber = RegistrationNumberValid,
    make = VehicleMakeValid,
    model = VehicleModelValid,
    title = None,
    firstName = None,
    lastName = None,
    address = None,
    disposeFlag = None,
    keeperEndDate = None,
    keeperChangeDate = None,
    suppressedV5Flag = None
  )

  private def vehicleAndKeeperLookupFormModel(userType: String) = VehicleAndKeeperLookupFormModel(
    replacementVRN = ReplacementVRN,
    referenceNumber = ReferenceNumberValid,
    registrationNumber = RegistrationNumberValid,
    postcode = KeeperPostCodeValid.get,
    userType = userType
  )

  private def captureCertificateDetailsModel = CaptureCertificateDetailsModel(
    prVrm = RegistrationNumberValid,
    certificate = ValidCertificate
  )


  private def captureCertificateDetailsFormModel = CaptureCertificateDetailsFormModel(
    certificateDate = CertificateDateValid,
    certificateDocumentCount = CertificateDocumentCountValid,
    certificateTime = CertificateTimeValid,
    certificateRegistrationMark = RegistrationNumberValid
  )

  private val TransactionId = "stubTransactionId"

  private def confirmFormModel = ConfirmFormModel(
    keeperEmail = Some(KeeperEmailValid),
    granteeConsent = GranteeConsentValid
  )

  private def businessDetailsModel = BusinessDetailsModel(
    name = TraderBusinessNameValid,
    contact = TraderBusinessContactValid,
    email = TraderBusinessEmailValid,
    address = addressWithoutUprn
  )
}
