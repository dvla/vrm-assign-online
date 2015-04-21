package controllers

import com.google.inject.Inject
import mappings.common.ErrorCodes
import models._
import org.joda.time.{DateTimeZone, DateTime}
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.data.{FormError, Form => PlayForm}
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClearTextClientSideSessionFactory, ClientSideSessionFactory}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase
import uk.gov.dvla.vehicles.presentation.common.model.{BruteForcePreventionModel, VehicleAndKeeperDetailsModel}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode.formatPostcode
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperDetailsDto, VehicleAndKeeperLookupService}
import utils.helpers.Config
import views.vrm_assign.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_assign.Payment._
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup._
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest

import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichForm

final class VehicleLookup @Inject()(implicit bruteForceService: BruteForcePreventionService,
                                    vehicleAndKeeperLookupService: VehicleAndKeeperLookupService,
                                    dateService: DateService,
                                    auditService2: audit2.AuditService,
                                    clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends VehicleLookupBase[VehicleAndKeeperLookupFormModel] {

  val unhandledVehicleAndKeeperLookupExceptionResponseCode = "VMPR6"

  override val form = PlayForm(
    VehicleAndKeeperLookupFormModel.Form.Mapping
  )
  override val responseCodeCacheKey: String = VehicleAndKeeperLookupResponseCodeCacheKey

  override def vrmLocked(bruteForcePreventionModel: BruteForcePreventionModel, formModel: VehicleAndKeeperLookupFormModel)
                        (implicit request: Request[_]): Result =
    addDefaultCookies(Redirect(routes.VrmLocked.present()), transactionId(formModel))

  override def microServiceError(t: Throwable, formModel: VehicleAndKeeperLookupFormModel)
                                (implicit request: Request[_]): Result =
    addDefaultCookies(Redirect(routes.MicroServiceError.present()), transactionId(formModel))

  override def presentResult(implicit request: Request[_]) =
    request.cookies.getModel[FulfilModel] match {
      case Some(fulfilModel) =>
        Ok(views.html.vrm_assign.vehicle_lookup(form)).discardingCookies(removeCookiesOnExit)
      case None =>
        Ok(views.html.vrm_assign.vehicle_lookup(form.fill()))
    }

  override def invalidFormResult(invalidForm: PlayForm[VehicleAndKeeperLookupFormModel])
                                (implicit request: Request[_]): Future[Result] = Future.successful {
    BadRequest(views.html.vrm_assign.vehicle_lookup(formWithReplacedErrors(invalidForm)))
  }

  // TODO need to combine vehicleLookupFailure and vehicleFoundResult as they are very similar after recent changes
  override def vehicleLookupFailure(responseCode: String, formModel: VehicleAndKeeperLookupFormModel)
                                   (implicit request: Request[_]): Result = {

    // need to record the current vrm from the form so put this into the
    // vehicleAndKeeperDetailsModel
    val vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel(
      registrationNumber = formatVrm(formModel.registrationNumber),
      make = None,
      model = None,
      title = None,
      firstName = None,
      lastName = None,
      address = None,
      disposeFlag = None,
      keeperEndDate = None,
      keeperChangeDate = None,
      suppressedV5Flag = None
    )

    val txnId = transactionId(formModel)

    // check whether the response code is a VMPR6 code, if so redirect to CaptureCertificateDetails
    // so it eventually redirects to DirectToPaper
    if (responseCode.startsWith(unhandledVehicleAndKeeperLookupExceptionResponseCode)) {
      if (formModel.userType == UserType_Keeper) {
        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.VehicleLookupToCaptureCertificateDetails,
          transactionId = txnId,
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
          rejectionCode = Some(responseCode)))

        addDefaultCookies(Redirect(routes.CaptureCertificateDetails.present()), txnId).
          withCookie(vehicleAndKeeperDetailsModel)
      } else {
        val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
        val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]
        if (storeBusinessDetails && businessDetailsModel.isDefined) {
          auditService2.send(AuditRequest.from(
            pageMovement = AuditRequest.VehicleLookupToConfirmBusiness,
            transactionId = txnId,
            timestamp = dateService.dateTimeISOChronology,
            vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
            businessDetailsModel = businessDetailsModel,
            rejectionCode = Some(responseCode)))

          addDefaultCookies(Redirect(routes.ConfirmBusiness.present()), txnId).
            withCookie(vehicleAndKeeperDetailsModel)
        } else {
          auditService2.send(AuditRequest.from(
            pageMovement = AuditRequest.VehicleLookupToCaptureActor,
            transactionId = txnId,
            timestamp = dateService.dateTimeISOChronology,
            vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
            rejectionCode = Some(responseCode)))

          addDefaultCookies(Redirect(routes.SetUpBusinessDetails.present()), txnId).
            withCookie(vehicleAndKeeperDetailsModel)
        }
      }
    } else {
      auditService2.send(AuditRequest.from(
        pageMovement = AuditRequest.VehicleLookupToVehicleLookupFailure,
        transactionId = txnId,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
        rejectionCode = Some(responseCode)))

      addDefaultCookies(Redirect(routes.VehicleLookupFailure.present()), txnId)
    }
  }

  override def vehicleFoundResult(vehicleAndKeeperDetailsDto: VehicleAndKeeperDetailsDto,
                                  formModel: VehicleAndKeeperLookupFormModel)
                                 (implicit request: Request[_]): Result = {

    val txnId = transactionId(formModel)

    if (!postcodesMatch(formModel.postcode, vehicleAndKeeperDetailsDto.keeperPostcode)) {

      val vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto)
      auditService2.send(AuditRequest.from(
        pageMovement = AuditRequest.VehicleLookupToVehicleLookupFailure,
        transactionId = txnId,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
        rejectionCode = Some(ErrorCodes.PostcodeMismatchErrorCode + " - vehicle_and_keeper_lookup_keeper_postcode_mismatch")))

      addDefaultCookies(Redirect(routes.VehicleLookupFailure.present()), txnId).
        withCookie(responseCodeCacheKey, "vehicle_and_keeper_lookup_keeper_postcode_mismatch")
    } else {
      val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
      val transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId)
      val vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto)

      if (formModel.userType == UserType_Keeper) {
        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.VehicleLookupToCaptureCertificateDetails,
          transactionId = txnId,
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel)))
        addDefaultCookies(Redirect(routes.CaptureCertificateDetails.present()), txnId).
          withCookie(VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto))
      } else {
        val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]
        if (storeBusinessDetails && businessDetailsModel.isDefined) {
          auditService2.send(AuditRequest.from(
            pageMovement = AuditRequest.VehicleLookupToConfirmBusiness,
            transactionId = txnId,
            timestamp = dateService.dateTimeISOChronology,
            vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
            businessDetailsModel = businessDetailsModel))
          addDefaultCookies(Redirect(routes.ConfirmBusiness.present()), txnId).
            withCookie(VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto))
        } else {
          auditService2.send(AuditRequest.from(
            pageMovement = AuditRequest.VehicleLookupToCaptureActor,
            transactionId = txnId,
            timestamp = dateService.dateTimeISOChronology,
            vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel)))
          addDefaultCookies(Redirect(routes.SetUpBusinessDetails.present()), txnId).
            withCookie(VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto))
        }
      }
    }
  }

  def back = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
  }

  private def transactionId(validForm: VehicleAndKeeperLookupFormModel): String = {
    val transactionTimestamp =
      DayMonthYear.from(new DateTime(dateService.now, DateTimeZone.forID("Europe/London"))).toDateTimeMillis.get
    val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp).drop(2) + " " +
      ISODateTimeFormat.hourMinuteSecond().print(transactionTimestamp)
    validForm.registrationNumber +
      isoDateTimeString.replace(" ", "").replace("-", "").replace(":", "").replace(".", "")
  }

  private def formWithReplacedErrors(form: PlayForm[VehicleAndKeeperLookupFormModel])(implicit request: Request[_]) =
    (form /: List(
      (VehicleRegistrationNumberId, "error.restricted.validVrnOnly"),
      (DocumentReferenceNumberId, "error.validDocumentReferenceNumber"),
      (PostcodeId, "error.restricted.validV5CPostcode"))) { (form, error) =>
      form.replaceError(error._1, FormError(
        key = error._1,
        message = error._2,
        args = Seq.empty
      ))
    }.distinctErrors

  // payment solve requires (for each day) a unique six digit number
  // use time from midnight in tenths of a second units
  private def calculatePaymentTransNo = {
    val milliSecondsFromMidnight = dateService.today.toDateTime.get.millisOfDay().get()
    val tenthSecondsFromMidnight = (milliSecondsFromMidnight / 100.0).toInt
    // prepend with zeros
    "%06d".format(tenthSecondsFromMidnight)
  }

  private def addDefaultCookies(result: Result, transactionId: String)
                               (implicit request: Request[_]): Result = result
    .withCookie(TransactionIdCacheKey, transactionId)
    .withCookie(PaymentTransNoCacheKey, calculatePaymentTransNo)

  private def postcodesMatch(formModelPostcode: String, dtoPostcode: Option[String]) = {
    dtoPostcode match {
      case Some(postcode) => {
        Logger.info("formModelPostcode = " + formModelPostcode + " dtoPostcode " + postcode)

        def formatPartialPostcode(postcode: String): String = {
          val SpaceCharDelimiter = " "
          val A99AA = "([A-Z][0-9][*]{3})".r
          val A099AA = "([A-Z][0][0-9][*]{3})".r
          val A999AA = "([A-Z][0-9]{2}[*]{3})".r
          val A9A9AA = "([A-Z][0-9][A-Z][*]{3})".r
          val AA99AA = "([A-Z]{2}[0-9][*]{3})".r
          val AA099AA = "([A-Z]{2}[0][0-9][*]{3})".r
          val AA999AA = "([A-Z]{2}[0-9]{2}[*]{3})".r
          val AA9A9AA = "([A-Z]{2}[0-9][A-Z][*]{3})".r

          postcode.toUpperCase.replace(SpaceCharDelimiter, "") match {
            case A99AA(p) => p.substring(0, 2)
            case A099AA(p) => p.substring(0, 1) + p.substring(2, 3)
            case A999AA(p) => p.substring(0, 3)
            case A9A9AA(p) => p.substring(0, 3)
            case AA99AA(p) => p.substring(0, 3)
            case AA099AA(p) => p.substring(0, 2) + p.substring(3, 4)
            case AA999AA(p) => p.substring(0, 4)
            case AA9A9AA(p) => p.substring(0, 4)
            case _ => formatPostcode(postcode)
          }
        }

        // strip the spaces before comparison
        formatPostcode(formModelPostcode).filterNot(" " contains _).toUpperCase() ==
          formatPartialPostcode(postcode).filterNot(" " contains _).toUpperCase()
      }
      case None => {
        Logger.info("formModelPostcode = " + formModelPostcode)
        formModelPostcode.isEmpty
      }
    }
  }
}