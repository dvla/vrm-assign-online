package controllers

import com.google.inject.Inject
import mappings.common.ErrorCodes
import models.{BusinessDetailsModel, CacheKeyPrefix, FulfilModel, IdentifierCacheKey, VehicleAndKeeperLookupFormModel}
import play.api.data.{FormError, Form => PlayForm}
import play.api.mvc.{Action, Request, Result}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase
import uk.gov.dvla.vehicles.presentation.common.model.MicroserviceResponseModel.MsResponseCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.{BruteForcePreventionModel, MicroserviceResponseModel, VehicleAndKeeperDetailsModel}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup
import vehicleandkeeperlookup.{VehicleAndKeeperLookupDetailsDto, VehicleAndKeeperLookupFailureResponse, VehicleAndKeeperLookupService}
import utils.helpers.Config
import views.vrm_assign.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_assign.Payment.PaymentTransNoCacheKey
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup.{DocumentReferenceNumberId, PostcodeId, ReplacementVRN, TransactionIdCacheKey, UserType_Keeper, VehicleRegistrationNumberId}
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest

import scala.concurrent.Future

final class VehicleLookup @Inject()(implicit bruteForceService: BruteForcePreventionService,
                                    vehicleAndKeeperLookupService: VehicleAndKeeperLookupService,
                                    dateService: DateService,
                                    auditService2: audit2.AuditService,
                                    clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends VehicleLookupBase[VehicleAndKeeperLookupFormModel] {

  override val form = PlayForm(
    VehicleAndKeeperLookupFormModel.Form.Mapping
  )
  override val responseCodeCacheKey: String = MsResponseCacheKey

  override def vrmLocked(bruteForcePreventionModel: BruteForcePreventionModel,
                         formModel: VehicleAndKeeperLookupFormModel)
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

    val trackingId = request.cookies.trackingId()
    auditService2.send(AuditRequest.from(
      pageMovement = AuditRequest.VehicleLookupToVehicleLookupFailure,
      transactionId = transactionId(formModel.registrationNumber),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
      rejectionCode = Some(ErrorCodes.VrmLockedErrorCode +
        VehicleLookupBase.RESPONSE_CODE_DELIMITER +
        VehicleLookupBase.RESPONSE_CODE_VRM_LOCKED)), trackingId
    )

    addDefaultCookies(Redirect(routes.VrmLocked.present()),
      transactionId(formModel.registrationNumber),
      TransactionIdCacheKey,
      PaymentTransNoCacheKey)
  }

  override def microServiceError(t: Throwable, formModel: VehicleAndKeeperLookupFormModel)
                                (implicit request: Request[_]): Result =
    addDefaultCookies(Redirect(routes.MicroServiceError.present()),
      transactionId(formModel.registrationNumber),
      TransactionIdCacheKey,
      PaymentTransNoCacheKey)

  override def presentResult(implicit request: Request[_]) = {
    request.cookies.getString(IdentifierCacheKey) match {
      case Some(c) =>
        Redirect(routes.VehicleLookup.ceg())
      case None =>
        logMessage(request.cookies.trackingId(), Info, "Presenting vehicle lookup view")
        vehicleLookup
    }
  }

  override def invalidFormResult(invalidForm: PlayForm[VehicleAndKeeperLookupFormModel])
                                (implicit request: Request[_]): Future[Result] = Future.successful {
    logMessage(request.cookies.trackingId(), Debug, "VehicleLookup.invalidFormResult" + invalidForm.errors)
    BadRequest(views.html.vrm_assign.vehicle_lookup(formWithReplacedErrors(invalidForm)))
  }

  override def vehicleLookupFailure(failure: VehicleAndKeeperLookupFailureResponse,
                                    formModel: VehicleAndKeeperLookupFormModel)
                                   (implicit request: Request[_]): Result = {

    val responseCode = failure.response
    logMessage(request.cookies.trackingId(), Debug, s"vehicleLookupFailure " + responseCode)

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

    val txnId = transactionId(formModel.registrationNumber)

    // check whether the response code is a VMPR6 code, if so redirect to microservice error page
    val trackingId = request.cookies.trackingId()
    if (responseCode.code.startsWith(VehicleLookupBase.RESPONSE_CODE_POSTCODE_MISMATCH)) {
      addDefaultCookies(Redirect(routes.MicroServiceError.present()),
        transactionId(formModel.registrationNumber),
        TransactionIdCacheKey,
        PaymentTransNoCacheKey)
    } else {
      auditService2.send(AuditRequest.from(
        pageMovement = AuditRequest.VehicleLookupToVehicleLookupFailure,
        transactionId = txnId,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
        rejectionCode = Some(s"${responseCode.code} - ${responseCode.message}")), trackingId
      )

      addDefaultCookies(Redirect(routes.VehicleLookupFailure.present()),
        txnId,
        TransactionIdCacheKey,
        PaymentTransNoCacheKey)
    }
  }

  override def vehicleFoundResult(vehicleAndKeeperDetailsDto: VehicleAndKeeperLookupDetailsDto,
                                  formModel: VehicleAndKeeperLookupFormModel)
                                 (implicit request: Request[_]): Result = {

    val txnId = transactionId(formModel.registrationNumber)
    val trackingId = request.cookies.trackingId()
    if (!postcodesMatch(formModel.postcode, vehicleAndKeeperDetailsDto.keeperPostcode)(trackingId)) {

      val vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto)
      auditService2.send(
        AuditRequest.from(
          pageMovement = AuditRequest.VehicleLookupToVehicleLookupFailure,
          transactionId = txnId,
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
          rejectionCode = Some(
            ErrorCodes.PostcodeMismatchErrorCode +
              VehicleLookupBase.RESPONSE_CODE_DELIMITER +
              VehicleLookupBase.RESPONSE_CODE_POSTCODE_MISMATCH
          )
        ), trackingId
      )

      addDefaultCookies(Redirect(routes.VehicleLookupFailure.present()),
        txnId,
        TransactionIdCacheKey,
        PaymentTransNoCacheKey).
        withCookie(MicroserviceResponseModel.content(MicroserviceResponse(code = "", message = VehicleLookupBase.RESPONSE_CODE_POSTCODE_MISMATCH)))
    } else {
      val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
      val vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto)

      if (formModel.userType == UserType_Keeper) {
        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.VehicleLookupToCaptureCertificateDetails,
          transactionId = txnId,
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel)), trackingId
        )
        addDefaultCookies(Redirect(routes.CaptureCertificateDetails.present()),
          txnId,
          TransactionIdCacheKey,
          PaymentTransNoCacheKey)
          .withCookie(VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto))
      } else {
        val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]
        if (storeBusinessDetails && businessDetailsModel.isDefined) {
          auditService2.send(AuditRequest.from(
            pageMovement = AuditRequest.VehicleLookupToConfirmBusiness,
            transactionId = txnId,
            timestamp = dateService.dateTimeISOChronology,
            vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
            businessDetailsModel = businessDetailsModel), trackingId
          )
          addDefaultCookies(Redirect(routes.ConfirmBusiness.present()),
            txnId,
            TransactionIdCacheKey,
            PaymentTransNoCacheKey)
            .withCookie(VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto))
        } else {
          auditService2.send(AuditRequest.from(
            pageMovement = AuditRequest.VehicleLookupToCaptureActor,
            transactionId = txnId,
            timestamp = dateService.dateTimeISOChronology,
            vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel)), trackingId
          )
          addDefaultCookies(Redirect(routes.SetUpBusinessDetails.present()),
            txnId,
            TransactionIdCacheKey,
            PaymentTransNoCacheKey)
            .withCookie(VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto))
        }
      }
    }
  }

  def back = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
  }

  val identifier = "CEG"
  def ceg = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Info, s"Presenting vehicle lookup view for identifier ${identifier}")
    vehicleLookup.withCookie(IdentifierCacheKey, identifier)
  }

  private def vehicleLookup(implicit request: Request[_]) = {
    request.cookies.getModel[FulfilModel] match {
      case Some(fulfilModel) =>
        Ok(views.html.vrm_assign.vehicle_lookup(form)).discardingCookies(removeCookiesOnExit)
      case None =>
        Ok(views.html.vrm_assign.vehicle_lookup(form.fill()))
    }
  }

  private def formWithReplacedErrors(form: PlayForm[VehicleAndKeeperLookupFormModel])(implicit request: Request[_]) =
    (form /: List(
      (ReplacementVRN, "error.restricted.validVrnOnly"),
      (VehicleRegistrationNumberId, "error.restricted.validVrnOnly"),
      (DocumentReferenceNumberId, "error.validDocumentReferenceNumber"),
      (PostcodeId, "error.restricted.validV5CPostcode"))) { (form, error) =>
      form.replaceError(error._1, FormError(
        key = error._1,
        message = error._2,
        args = Seq.empty
      ))
    }.distinctErrors

}