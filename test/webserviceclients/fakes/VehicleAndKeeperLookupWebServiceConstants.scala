package webserviceclients.fakes

import play.api.http.Status.{NOT_FOUND, OK, SERVICE_UNAVAILABLE}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup
import vehicleandkeeperlookup.VehicleAndKeeperLookupDetailsDto
import vehicleandkeeperlookup.VehicleAndKeeperLookupFailureResponse
import vehicleandkeeperlookup.VehicleAndKeeperLookupSuccessResponse
import views.vrm_assign.VehicleLookup.UserType_Keeper
import webserviceclients.fakes.AddressLookupServiceConstants.PostcodeValid

object VehicleAndKeeperLookupWebServiceConstants {

  final val ReplacementVRN = "ABC123"
  final val RegistrationNumberValid = "AB12AWR"
  final val RegistrationNumberWithSpaceValid = "AB12 AWR"
  final val ReferenceNumberValid = "12345678910"
  final val TransactionIdValid = "ABC123123123123"
  final val PaymentTransNoValid = "123456"

  def VehicleMakeValid = Some("Alfa Romeo")

  def VehicleModelValid = Some("Alfasud ti")

  final val KeeperNameValid = "Keeper Name"
  final val KeeperUprnValid = 10123456789L
  final val ConsentValid = "true"
  final val KeeperConsentValid = UserType_Keeper
  final val BusinessConsentValid = "Business"
  final val KeeperPostcodeValid = PostcodeValid
  final val KeeperPostcodeValidForMicroService = "SA11AA"

  def KeeperTitleValid = Some("MR")

  def KeeperLastNameValid = Some("JONES")

  def KeeperFirstNameValid = Some("DAVID")

  def KeeperAddressLine1Valid = Some("1 High Street")

  def KeeperAddressLine2Valid = Some("Skewen")

  def KeeperAddressLine3Valid = None

  def KeeperAddressLine4Valid = None

  def KeeperPostTownValid = Some("Swansea")

  def KeeperPostCodeValid = Some("SA11AA")

  final val RecordMismatch = MicroserviceResponse(
    code = "200",
    message = "vehicle_and_keeper_lookup_document_record_mismatch"
  )
  final val NoKeeper = MicroserviceResponse("200", "vrm_assign_eligibility_no_keeper_failure")

  private def vehicleAndKeeperDetails = VehicleAndKeeperLookupDetailsDto(registrationNumber = RegistrationNumberValid,
    vehicleMake = VehicleMakeValid,
    vehicleModel = VehicleModelValid,
    keeperTitle = KeeperTitleValid,
    keeperFirstName = KeeperFirstNameValid,
    keeperLastName = KeeperLastNameValid,
    keeperAddressLine1 = KeeperAddressLine1Valid,
    keeperAddressLine2 = KeeperAddressLine2Valid,
    keeperAddressLine3 = KeeperAddressLine3Valid,
    keeperAddressLine4 = KeeperAddressLine4Valid,
    keeperPostTown = KeeperPostTownValid,
    keeperPostcode = KeeperPostCodeValid,
    disposeFlag = None,
    keeperEndDate = None,
    keeperChangeDate = None,
    suppressedV5Flag = None
  )

  val vehicleAndKeeperDetailsResponseSuccess: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
    VehicleAndKeeperLookupSuccessResponse]]) =
    (OK, Some(Right(VehicleAndKeeperLookupSuccessResponse(Some(vehicleAndKeeperDetails)))))


  val vehicleAndKeeperDetailsResponseVRMNotFound: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
    VehicleAndKeeperLookupSuccessResponse]]) =
    (NOT_FOUND,
      Some(Left(VehicleAndKeeperLookupFailureResponse(
        MicroserviceResponse("200", "vehicle_lookup_vrm_not_found")))
      )
    )

  val vehicleAndKeeperDetailsResponseDocRefNumberNotLatest: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
    VehicleAndKeeperLookupSuccessResponse]]) =
    (NOT_FOUND, Some(Left(VehicleAndKeeperLookupFailureResponse(RecordMismatch))))

  val vehicleAndKeeperDetailsResponseNotFoundResponseCode: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
    VehicleAndKeeperLookupSuccessResponse]]) =
    (OK, Some(Right(VehicleAndKeeperLookupSuccessResponse(None))))

  val vehicleAndKeeperDetailsServerDown: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
    VehicleAndKeeperLookupSuccessResponse]]) =
    (SERVICE_UNAVAILABLE, None)

  val vehicleAndKeeperDetailsNoResponse: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                             VehicleAndKeeperLookupSuccessResponse]]) = (OK, None)
}
