package controllers

import com.google.inject.Inject
import models._
import play.api.data.Form
import play.api.mvc.Result
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_assign.ConfirmBusiness._
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest

final class ConfirmBusiness @Inject()(
                                       auditService2: audit2.AuditService
                                       )
                                     (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                      config: Config,
                                      dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService)
  extends Controller {

  def presentOld = Action { implicit request =>

    (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[SetupBusinessDetailsFormModel],
      request.cookies.getModel[BusinessDetailsModel],
      request.cookies.getModel[FulfilModel]) match {
      case (Some(vehicleAndKeeperLookupForm), Some(vehicleAndKeeper),
        Some(setupBusinessDetailsFormModel), Some(businessDetailsModel), None) =>
        val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)

        val viewModel = ConfirmBusinessViewModel(vehicleAndKeeper, Some(businessDetailsModel))
        Ok(views.html.vrm_assign.confirm_business(viewModel))
      case _ =>
//        Redirect(routes.BusinessChooseYourAddress.present())
        Redirect(routes.SetUpBusinessDetails.present())
    }
  }

  def present = Action { implicit request =>
/*
    println("ConfirmBusiness - present cookies >>>>>>>>>>>>>>>>>>>>>>>>>>")

    request.cookies.getModel[VehicleAndKeeperLookupFormModel] match {
      case Some(value) => println(s"ConfirmBusiness - VehicleAndKeeperLookupFormModel = $value")
      case _ => println(s"ConfirmBusiness - VehicleAndKeeperLookupFormModel MISSING!!!!!!!!!")
    }
    request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
      case Some(value) => println(s"ConfirmBusiness - VehicleAndKeeperDetailsModel = $value")
      case _ => println(s"ConfirmBusiness - VehicleAndKeeperDetailsModel MISSING!!!!!!!!!")
    }
    request.cookies.getModel[SetupBusinessDetailsFormModel] match {
      case Some(value) => println(s"ConfirmBusiness - SetupBusinessDetailsFormModel = $value")
      case _ => println(s"ConfirmBusiness - SetupBusinessDetailsFormModel MISSING!!!!!!!!!")
    }
    request.cookies.getModel[BusinessDetailsModel] match {
      case Some(value) => println(s"ConfirmBusiness - BusinessDetailsModel = $value")
      case _ => println(s"ConfirmBusiness - BusinessDetailsModel MISSING!!!!!!!!!")
    }
*/

    (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[SetupBusinessDetailsFormModel],
      request.cookies.getModel[BusinessDetailsModel],
      request.cookies.getModel[FulfilModel]) match {
        case (Some(vehicleAndKeeperLookupForm), Some(vehicleAndKeeper),
          Some(setupBusinessDetailsFormModel), Some(businessDetailsModel), None) =>

          val viewModel = ConfirmBusinessViewModel(vehicleAndKeeper, Some(businessDetailsModel))
          Ok(views.html.vrm_assign.confirm_business(viewModel))
        case _ =>
//          println("ConfirmBusiness - BOOM redirecting to setUpBusinessDetails>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
  //        Redirect(routes.BusinessChooseYourAddress.present())
          Redirect(routes.SetUpBusinessDetails.present())
      }
  }

  def submit = Action { implicit request =>
//    println("ConfirmBusiness - submit called >>>>>>>>>>>>>>>>>")
    handleValid()
  }

  def back = Action { implicit request =>
    // TODO: ian fix me
//    request.cookies.getModel[EnterAddressManuallyModel] match {
//      case Some(enterAddressManuallyModel) => Redirect(routes.EnterAddressManually.present())
//      case None => Redirect(routes.BusinessChooseYourAddress.present())
//    }
//    Redirect(routes.BusinessChooseYourAddress.present())
    Redirect(routes.SetUpBusinessDetails.present())
  }

  private def handleValid()(implicit request: Request[_]): Result = {
    val happyPath = request.cookies.getModel[VehicleAndKeeperLookupFormModel].map {
      vehicleAndKeeperLookup =>
        (request.cookies.getString(TransactionIdCacheKey),
          request.cookies.getModel[VehicleAndKeeperDetailsModel],
          request.cookies.getModel[BusinessDetailsModel],
          request.cookies.getModel[EnterAddressManuallyModel],
          request.cookies.getModel[BusinessChooseYourAddressFormModel],
          request.cookies.getModel[SetupBusinessDetailsFormModel]
          ) match {
          case (transactionId, vehicleAndKeeperDetailsModel, businessDetailsModel,
            enterAddressManuallyModel, businessChooseYourAddressFormModel, setupBusinessDetailsFormModel) =>

            auditService2.send(AuditRequest.from(
              pageMovement = AuditRequest.ConfirmBusinessToCaptureCertificateDetails,
              transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
              timestamp = dateService.dateTimeISOChronology,
              vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
              businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))
            Redirect(routes.CaptureCertificateDetails.present()).
              withCookie(enterAddressManuallyModel).
              withCookie(businessChooseYourAddressFormModel).
              withCookie(businessDetailsModel).
              withCookie(setupBusinessDetailsFormModel)
        }
    }
    val sadPath = Redirect(routes.Error.present("user went to ConfirmBusiness handleValid without VehicleAndKeeperLookupFormModel cookie"))
    happyPath.getOrElse(sadPath)
  }

  def exit = Action { implicit request =>
    auditService2.send(AuditRequest.from(
      pageMovement = AuditRequest.ConfirmBusinessToExit,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }
}