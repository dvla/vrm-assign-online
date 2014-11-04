package controllers

import com.google.inject.Inject
import models._
import org.joda.time.format.ISODateTimeFormat
import play.api.data.{FormError, Form => PlayForm}
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase.{LookupResult, VehicleFound, VehicleNotFound}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode.formatPostcode
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import utils.helpers.Config
import views.vrm_assign.RelatedCacheKeys
import views.vrm_assign.VehicleLookup._
import webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperLookupService}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import audit.{AuditMessage, AuditService}
import scala.Some
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase.VehicleFound
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase.VehicleNotFound
import play.api.mvc.Call
import mappings.common.ErrorCodes
import views.vrm_assign.ConfirmBusiness._
import views.vrm_assign.ConfirmBusiness.StoreBusinessDetailsCacheKey
import scala.Some
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase.VehicleNotFound
import play.api.mvc.Call

final class VehicleLookup @Inject()(val bruteForceService: BruteForcePreventionService,
                                    vehicleAndKeeperLookupService: VehicleAndKeeperLookupService,
                                    dateService: DateService, auditService: AuditService)
                                   (implicit val clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends VehicleLookupBase {

  override val vrmLocked: Call = routes.VrmLocked.present()
  override val microServiceError: Call = routes.MicroServiceError.present()
  override val vehicleLookupFailure: Call = routes.VehicleLookupFailure.present()
  override val responseCodeCacheKey: String = VehicleAndKeeperLookupResponseCodeCacheKey

  override type Form = VehicleAndKeeperLookupFormModel

  private[controllers] val form = PlayForm(
    VehicleAndKeeperLookupFormModel.Form.Mapping
  )

  def present = Action { implicit request =>
    Ok(views.html.vrm_assign.vehicle_lookup(form.fill())).
      discardingCookies(RelatedCacheKeys.VehicleAndKeeperLookupSet)
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => Future.successful {
        BadRequest(views.html.vrm_assign.vehicle_lookup(formWithReplacedErrors(invalidForm)))
      },
      validForm => {
        bruteForceAndLookup(
          validForm.registrationNumber,
          validForm.referenceNumber,
          validForm)
          .map(_.withCookie(TransactionIdCacheKey, transactionId(validForm)))
      }
    )
  }

  def back = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
  }

  override protected def callLookupService(trackingId: String, form: Form)(implicit request: Request[_]): Future[LookupResult] =
    vehicleAndKeeperLookupService.invoke(VehicleAndKeeperDetailsRequest.from(form), trackingId).map { response =>
      response.responseCode match {
        case Some(responseCode) =>

          auditService.send(AuditMessage.from(
            pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
            transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse("no-transaction-id-cookie"),
            timestamp = dateService.dateTimeISOChronology,
            vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
            rejectionCode = Some(responseCode)))

          VehicleNotFound(responseCode.split(" - ")(1))

        case None =>
          response.vehicleAndKeeperDetailsDto match {
            case Some(dto) if !formatPostcode(form.postcode).equals(formatPostcode(dto.keeperPostcode.get)) =>

              auditService.send(AuditMessage.from(
                pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
                transactionId = request.cookies.getString(TransactionIdCacheKey).get,
                timestamp = dateService.dateTimeISOChronology,
                vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
                rejectionCode = Some(ErrorCodes.PostcodeMismatchErrorCode + " - vehicle_and_keeper_lookup_keeper_postcode_mismatch")))

              VehicleNotFound("vehicle_and_keeper_lookup_keeper_postcode_mismatch")

            case Some(dto) =>

              val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
              val transactionId = request.cookies.getString(TransactionIdCacheKey).get // TOD will it exist? option?

              val vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel.from(dto)

              if (form.userType == UserType_Keeper) {
                auditService.send(AuditMessage.from(
                  pageMovement = AuditMessage.VehicleLookupToCaptureCertificateDetails,
                  transactionId = transactionId,
                  timestamp = dateService.dateTimeISOChronology,
                  vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel)))
                routes.CaptureCertificateDetails.present()
              } else {
                if (storeBusinessDetails) {
                  val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]
                  auditService.send(AuditMessage.from(
                    pageMovement = AuditMessage.VehicleLookupToConfirmBusiness,
                    transactionId = transactionId,
                    timestamp = dateService.dateTimeISOChronology,
                    vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
                    businessDetailsModel = businessDetailsModel))
                  routes.ConfirmBusiness.present()
                } else {
                  auditService.send(AuditMessage.from(
                    pageMovement = AuditMessage.VehicleLookupToCaptureActor,
                    transactionId = transactionId,
                    timestamp = dateService.dateTimeISOChronology,
                    vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel)))
                  routes.SetUpBusinessDetails.present()
                }
              }

              VehicleFound(Redirect(routes.CaptureCertificateDetails.present()).
                withCookie(VehicleAndKeeperDetailsModel.from(dto)))

            case None =>
              throw new RuntimeException("No Dto in vehicleAndKeeperLookupService response")
          }
      }
    }

  private def transactionId(validForm: VehicleAndKeeperLookupFormModel): String = {
    val transactionTimestamp = dateService.today.toDateTimeMillis.get
    val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp) + " " +
      ISODateTimeFormat.hourMinuteSecondMillis().print(transactionTimestamp)
    validForm.registrationNumber +
      isoDateTimeString.replace(" ", "").replace("-", "").replace(":", "").replace(".", "")
  }

  private def formWithReplacedErrors(form: PlayForm[VehicleAndKeeperLookupFormModel])(implicit request: Request[_]) =
    (form /: List(
      (VehicleRegistrationNumberId, "error.restricted.validVrnOnly"),
      (DocumentReferenceNumberId, "error.validDocumentReferenceNumber"),
      (PostcodeId, "error.restricted.validPostcode"))) { (form, error) =>
      form.replaceError(error._1, FormError(
        key = error._1,
        message = error._2,
        args = Seq.empty
      ))
    }.distinctErrors

}