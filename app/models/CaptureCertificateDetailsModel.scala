package models

import org.joda.time.DateTime
import play.api.libs.json.{Format, JsError, JsObject, Json, JsString, JsSuccess, JsUndefined, JsValue}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm
import views.vrm_assign.CaptureCertificateDetails.CaptureCertificateDetailsCacheKey

sealed trait Certificate
object Certificate {
  case class Expired(expiryDate: DateTime) extends Certificate
  case class ExpiredWithFee(expiryDate: DateTime, outstandingFee: Int, fmtFee: String) extends Certificate
  case object Unknown extends Certificate
  case class Valid(expiryDate: DateTime) extends Certificate

  implicit val JsonFormat = new Format[Certificate] {
    implicit val expired = Json.format[Expired]
    implicit val expiredWithFee = Json.format[ExpiredWithFee]
    implicit val valid = Json.format[Valid]

    def reads(json: JsValue) = (json \ "class").as[String] match {
      case "Expired"         => Json.fromJson[Expired](json \ "data")
      case "ExpiredWithFee"  => Json.fromJson[ExpiredWithFee](json \ "data")
      case "Unknown"         => JsSuccess(Unknown)
      case "Valid"           => Json.fromJson[Valid](json \ "data")
      case _                 => JsError(s"Unknown format")
    }

    def writes(o: Certificate) = {
      val (certType, certificate) = o match {
        case c: Expired        => ("Expired", Json.toJson(c))
        case c: ExpiredWithFee => ("ExpiredWithFee", Json.toJson(c))
        case Unknown           => ("Unknown", JsUndefined("undefined"))
        case c: Valid          => ("Valid", Json.toJson(c))
      }
      JsObject(Seq("class" -> JsString(certType), "data" -> certificate))
    }
  }
}

final case class CaptureCertificateDetailsModel(prVrm: String, certificate: Certificate)

object CaptureCertificateDetailsModel {

  def from(prVrm: String, certificate: Certificate) =
    CaptureCertificateDetailsModel(formatVrm(prVrm), certificate)

  implicit val JsonFormat = Json.format[CaptureCertificateDetailsModel]
  implicit val Key = CacheKey[CaptureCertificateDetailsModel](CaptureCertificateDetailsCacheKey)
}
