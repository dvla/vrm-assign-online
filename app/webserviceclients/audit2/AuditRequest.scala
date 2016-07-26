package webserviceclients.audit2

import models.BusinessDetailsModel
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.PaymentModel
import models.VehicleAndKeeperLookupFormModel
import play.api.libs.json.JsBoolean
import play.api.libs.json.JsNull
import play.api.libs.json.JsNumber
import play.api.libs.json.Json
import play.api.libs.json.JsString
import play.api.libs.json.JsValue
import play.api.libs.json.Json.obj
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.Writes
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

case class AuditRequest(name: String, serviceType: String, data: Seq[(String, Any)])

object AuditRequest {

  // service types
  final val AuditServiceType = "PR Assign"

  // page movement names
  final val VehicleLookupToCaptureCertificateDetails = "VehicleLookupToCaptureCertificateDetails"
  final val VehicleLookupToConfirmBusiness = "VehicleLookupToConfirmBusiness"
  final val VehicleLookupToCaptureActor = "VehicleLookupToCaptureActor"
  final val VehicleLookupToVehicleLookupFailure = "VehicleLookupToVehicleLookupFailure"
  final val VehicleLookupToExit = "VehicleLookupToExit"
  final val VehicleLookupToMicroServiceError = "VehicleLookupToMicroServiceError"

  final val CaptureActorToConfirmBusiness = "CaptureActorToConfirmBusiness"
  final val CaptureActorToExit = "CaptureActorToExit"

  final val ConfirmBusinessToCaptureCertificateDetails = "ConfirmBusinessToCaptureCertificateDetails"
  final val ConfirmBusinessToExit = "ConfirmBusinessToExit"

  final val CaptureCertificateDetailsToConfirm = "CaptureCertificateDetailsToConfirm"
  final val CaptureCertificateDetailsToMicroServiceError = "CaptureCertificateDetailsToMicroServiceError"
  final val CaptureCertificateDetailsToExit = "CaptureCertificateDetailsToExit"
  final val CaptureCertificateDetailsToCaptureCertificateDetailsFailure =
    "CaptureCertificateDetailsToCaptureCertificateDetailsFailure"

  //final val ConfirmToPayment = "ConfirmToPayment"
  final val ConfirmToSuccess = "ConfirmToSuccess"
  final val ConfirmToExit = "ConfirmToExit"
  final val ConfirmToFulfilFailure = "ConfirmToFulfilFailure"
  final val ConfirmToFeesDue = "ConfirmToFeesDue"
  final val FeesDueToPay = "FeesDueToPay"


  final val PaymentToSuccess = "PaymentToSuccess"
  final val PaymentToPaymentNotAuthorised = "PaymentToPaymentNotAuthorised"
  final val PaymentToPaymentFailure = "PaymentToPaymentFailure"
  final val PaymentToExit = "PaymentToExit"
  final val PaymentToMicroServiceError = "PaymentToMicroServiceError"

  implicit val jsonWrites = new Writes[Seq[(String, Any)]] {
    def writes(o: Seq[(String, Any)]): JsValue = obj(
      o.map {
        case (key: String, value: Any) =>
          val ret: (String, JsValueWrapper) = value match {
            case asString: String => key -> JsString(asString)
            case asInt: Int => key -> JsNumber(asInt)
            case asDouble: Double => key -> JsNumber(asDouble)
            case None => key -> JsNull
            case asBool: Boolean => key -> JsBoolean(asBool)
            case _ => throw new RuntimeException("no match, you need to tell it how to cast this type to json")
          }
          ret
      }: _*
    )
  }

  implicit val auditMessageFormat = Json.writes[AuditRequest]

  def from(pageMovement: String,
           transactionId: String,
           timestamp: String,
           documentReferenceNumber: Option[String] = None,
           vehicleAndKeeperDetailsModel: Option[VehicleAndKeeperDetailsModel] = None,
           captureCertificateDetailFormModel: Option[CaptureCertificateDetailsFormModel] = None,
           captureCertificateDetailsModel: Option[CaptureCertificateDetailsModel] = None,
           keeperEmail: Option[String] = None,
           businessDetailsModel: Option[BusinessDetailsModel] = None,
           paymentModel: Option[PaymentModel] = None,
           rejectionCode: Option[String] = None) = {
    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", timestamp))
      val documentReferenceNumberOpt = documentReferenceNumber.map(ref => ("documentReferenceNumber", ref))
      val vehicleAndKeeperDetailsModelOptSeq = VehicleAndKeeperDetailsModelOptSeq.from(vehicleAndKeeperDetailsModel)
      val captureCertificateDetailsFormModelOpt =
        CaptureCertificateDetailsFormModelOptSeq.from(captureCertificateDetailFormModel)
      val captureCertificateDetailsModelOpt = CaptureCertificateDetailsModelOptSeq.from(captureCertificateDetailsModel)
      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(businessDetailsModel)
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val paymentModelOptSeq = PaymentModelOptSeq.from(paymentModel)
      val rejectionCodeOpt = rejectionCode.map(rejectionCode => ("rejectionCode", rejectionCode))

      (Seq(
        transactionIdOpt,
        timestampOpt,
        documentReferenceNumberOpt,
        keeperEmailOpt,
        rejectionCodeOpt
      ) ++ vehicleAndKeeperDetailsModelOptSeq ++ businessDetailsModelOptSeq
        ++ captureCertificateDetailsFormModelOpt ++ captureCertificateDetailsModelOpt ++ paymentModelOptSeq).flatten
    }
    AuditRequest(pageMovement, AuditServiceType, data)
  }
}
