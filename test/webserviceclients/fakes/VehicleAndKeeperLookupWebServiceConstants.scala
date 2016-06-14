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
  final val VehicleMakeValid = Some("Alfa Romeo")
  final val VehicleModelValid = Some("Alfasud ti")
  final val BusinessConsentValid = "Business"

  final val KeeperConsentValid = UserType_Keeper
  final val KeeperPostcodeValid = PostcodeValid
  final val KeeperPostcodeValidForMicroService = "SA11AA"
  final val KeeperTitleValid = Some("MR")
  final val KeeperLastNameValid = Some("JONES")
  final val KeeperFirstNameValid = Some("DAVID")
  final val KeeperAddressLine1Valid = Some("1 High Street")
  final val KeeperAddressLine2Valid = Some("Skewen")
  final val KeeperAddressLine3Valid = None
  final val KeeperAddressLine4Valid = None
  final val KeeperPostTownValid = Some("Swansea")
  final val KeeperPostCodeValid = Some("SA11AA")

  final val RecordMismatch = MicroserviceResponse(
    code = "200",
    message = "vehicle_and_keeper_lookup_document_record_mismatch"
  )

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

  val vehicleAndKeeperLookupUnhandledExceptionResponseCode = "VMPR6"

  def vehicleAndKeeperDetailsResponseUnhandledException: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                  VehicleAndKeeperLookupSuccessResponse]]) =
    (NOT_FOUND, Some(Left(VehicleAndKeeperLookupFailureResponse(
      MicroserviceResponse(code = vehicleAndKeeperLookupUnhandledExceptionResponseCode, message = "unhandled_exception")
    ))))

  val vehicleAndKeeperDetailsResponseSuccess: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
    VehicleAndKeeperLookupSuccessResponse]]) =
    (OK, Some(Right(VehicleAndKeeperLookupSuccessResponse(Some(vehicleAndKeeperDetails)))))


  val vehicleAndKeeperDetailsResponseVRMNotFound: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
    VehicleAndKeeperLookupSuccessResponse]]) =
    (NOT_FOUND,
      Some(Left(VehicleAndKeeperLookupFailureResponse(
        MicroserviceResponse("VMPR1", "vehicle_and_keeper_lookup_vrm_not_found")))
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
