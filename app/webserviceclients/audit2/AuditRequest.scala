package webserviceclients.audit2

import audit1._
import models._
import play.api.libs.json.Json._
import play.api.libs.json._

case class AuditRequest(name: String, serviceType: String, data: Seq[(String, Any)])

object AuditRequest {

  // service types
  final val AuditServiceType = "PR Assign"

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
      }.toSeq: _*
    )
  }

  implicit val auditMessageFormat = Json.writes[AuditRequest]

  def from(pageMovement: String,
           transactionId: String,
           timestamp: String,
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
      val vehicleAndKeeperDetailsModelOptSeq = VehicleAndKeeperDetailsModelOptSeq.from(vehicleAndKeeperDetailsModel)
      val captureCertificateDetailsFormModelOpt = CaptureCertificateDetailsFormModelOptSeq.from(captureCertificateDetailFormModel)
      val captureCertificateDetailsModelOpt = CaptureCertificateDetailsModelOptSeq.from(captureCertificateDetailsModel)
      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(businessDetailsModel)
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val paymentModelOptSeq = PaymentModelOptSeq.from(paymentModel)
      val rejectionCodeOpt = rejectionCode.map(rejectionCode => ("rejectionCode", rejectionCode))

      (Seq(
        transactionIdOpt,
        timestampOpt,
        keeperEmailOpt,
        rejectionCodeOpt
      ) ++ vehicleAndKeeperDetailsModelOptSeq ++ businessDetailsModelOptSeq
        ++ captureCertificateDetailsFormModelOpt ++ captureCertificateDetailsModelOpt ++ paymentModelOptSeq).flatten
    }
    AuditRequest(pageMovement, AuditServiceType, data)
  }
}