package controllers

import javax.inject.Inject

import models._
import play.api.data.Form
import play.api.data.FormError
import play.api.i18n.Lang
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Request
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSession
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichForm
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.AddressLookupService
import utils.helpers.Config
import views.html.vrm_assign.business_choose_your_address
import views.vrm_assign.BusinessChooseYourAddress.AddressSelectId
import views.vrm_assign.EnterAddressManually.EnterAddressManuallyCacheKey
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup._
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class BusinessChooseYourAddress @Inject()(
                                                 addressLookupService: AddressLookupService,
                                                 auditService2: audit2.AuditService
                                                 )
                                               (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                config: Config,
                                                dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  private[controllers] val form = Form(BusinessChooseYourAddressFormModel.Form.Mapping)

  def present = Action.async { implicit request =>
    (request.cookies.getModel[SetupBusinessDetailsFormModel], request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[FulfilModel]) match {
      case (Some(setupBusinessDetailsForm), Some(vehicleAndKeeperDetails), None) =>
        val viewModel = BusinessChooseYourAddressViewModel(setupBusinessDetailsForm, vehicleAndKeeperDetails)
        val session = clientSideSessionFactory.getSession(request.cookies)
        fetchAddresses(setupBusinessDetailsForm)(session, request2lang).map { addresses =>
          if (config.ordnanceSurveyUseUprn) Ok(views.html.vrm_assign.business_choose_your_address(viewModel, form.fill(), addresses))
          else Ok(views.html.vrm_assign.business_choose_your_address(viewModel, form.fill(), index(addresses)))
        }
      case _ => Future.successful {
        Redirect(routes.SetUpBusinessDetails.present())
      }
    }
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        (request.cookies.getModel[SetupBusinessDetailsFormModel], request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
          case (Some(setupBusinessDetailsFormModel), Some(vehicleAndKeeperDetailsModel)) =>
            val viewModel = BusinessChooseYourAddressViewModel(setupBusinessDetailsFormModel, vehicleAndKeeperDetailsModel)
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            fetchAddresses(setupBusinessDetailsFormModel).map { addresses =>
              if (config.ordnanceSurveyUseUprn)
                BadRequest(business_choose_your_address(viewModel,
                  formWithReplacedErrors(invalidForm),
                  addresses)
                )
              else
                BadRequest(business_choose_your_address(viewModel,
                  formWithReplacedErrors(invalidForm),
                  index(addresses))
                )
            }
          case _ => Future.successful {
            Redirect(routes.SetUpBusinessDetails.present())
          }
        },
      validForm =>
        request.cookies.getModel[SetupBusinessDetailsFormModel] match {
          case Some(setupBusinessDetailsForm) =>
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            if (config.ordnanceSurveyUseUprn) {
              lookupUprn(validForm,
                setupBusinessDetailsForm.name,
                setupBusinessDetailsForm.contact,
                setupBusinessDetailsForm.email)
            } else {
              lookupAddressByPostcodeThenIndex(validForm, setupBusinessDetailsForm)
            }
          case None => Future.successful {
            Redirect(routes.SetUpBusinessDetails.present())
          }
        }
    )
  }

  def exit = Action { implicit request =>
    auditService2.send(AuditRequest.from(
      pageMovement = AuditRequest.CaptureActorToExit,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }

  private def index(addresses: Seq[(String, String)]) = {
    addresses.map { case (uprn, address) => address}. // Extract the address.
      zipWithIndex. // Add an index for each address
      map { case (address, index) => (index.toString, address)} // Flip them around so index comes first.
  }

  private def formWithReplacedErrors(form: Form[BusinessChooseYourAddressFormModel])(implicit request: Request[_]) =
    form.replaceError(AddressSelectId, "error.required",
      FormError(
        key = AddressSelectId,
        message = "vrm_assign_businessChooseYourAddress.address.required",
        args = Seq.empty)).
      distinctErrors

  private def fetchAddresses(model: SetupBusinessDetailsFormModel)(implicit session: ClientSideSession, lang: Lang) =
    addressLookupService.fetchAddressesForPostcode(postcode = model.postcode, trackingId = session.trackingId, showBusinessName = Some(true))

  private def lookupUprn(model: BusinessChooseYourAddressFormModel, businessName: String, businessContact: String, businessEmail: String)
                        (implicit request: Request[_], session: ClientSideSession) = {
    val lookedUpAddress = addressLookupService.fetchAddressForUprn(model.uprnSelected.toString, session.trackingId)
    lookedUpAddress.map {
      case Some(addressViewModel) =>
        nextPage(model, businessName, businessContact, businessEmail, addressViewModel)
      case None => Redirect(routes.UprnNotFound.present())
    }
  }

  private def lookupAddressByPostcodeThenIndex(model: BusinessChooseYourAddressFormModel, setupBusinessDetailsForm: SetupBusinessDetailsFormModel)
                                              (implicit request: Request[_], session: ClientSideSession) = {
    fetchAddresses(setupBusinessDetailsForm)(session, request2lang).map { addresses =>
      val indexSelected = model.uprnSelected.toInt
      if (indexSelected < addresses.length) {
        val lookedUpAddresses = index(addresses)
        val lookedUpAddress = lookedUpAddresses(indexSelected) match {
          case (index, address) => address
        }
        val addressModel = AddressModel(uprn = None, address = lookedUpAddress.split(","))
        nextPage(model, setupBusinessDetailsForm.name, setupBusinessDetailsForm.contact, setupBusinessDetailsForm.email, addressModel)
      }
      else {
        // Guard against IndexOutOfBoundsException
        Redirect(routes.UprnNotFound.present())
      }
    }
  }

  private def nextPage(model: BusinessChooseYourAddressFormModel, businessName: String, businessContact: String,
                       businessEmail: String, addressModel: AddressModel)
                      (implicit request: Request[_], session: ClientSideSession) = {
    val businessDetailsModel = BusinessDetailsModel(name = businessName,
      contact = businessContact,
      email = businessEmail,
      address = addressModel.formatPostcode)
    /* The redirect is done as the final step within the map so that:
     1) we are not blocking threads
     2) the browser does not change page before the future has completed and written to the cache. */

    auditService2.send(AuditRequest.from(
      pageMovement = AuditRequest.CaptureActorToConfirmBusiness,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

    Redirect(routes.ConfirmBusiness.present()).
      discardingCookie(EnterAddressManuallyCacheKey).
      withCookie(model).
      withCookie(businessDetailsModel)
  }
}