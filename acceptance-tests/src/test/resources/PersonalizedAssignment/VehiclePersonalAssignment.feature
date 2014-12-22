@HappyPath
Feature: Assignment of Vehicle

  Background:
    Given that I have started the PR Assign Service

  Scenario Outline: Keeper Acting and Fees Due
    Given I visit assign web portal
    When I enter data in the <VehicleRegistrationNumber>,<DocRefID> and <Postcode> for a vehicle that is eligible for retention
    And I indicate that the keeper is acting
    And enter <CertificateIdBox1>,<CertificateIdBox2>,<CertificateIdBox3>,<CertificateIdBox4>  and <RegistrationNumber>
    Then the enter confirm details page is displayed and the payment required section is shown
  Examples:
    | VehicleRegistrationNumber | DocRefID      | Postcode | CertificateIdBox1 | CertificateIdBox2 | CertificateIdBox3 | CertificateIdBox4 | RegistrationNumber |
    | "DD22"                    | "11111111111" | "SA11AA" | "1"               | "234567"          | "891234"          | "56"              | "ABC123"           |


  Scenario Outline: Invalid Data in Vehicle Registration Number, Doc Ref ID and Postcode
    Given I visit assign web portal
    When I enter invalid data in the <VehicleRegistrationNumber>,<DocRefID> and <Postcode> fields
    Then the error messages for invalid data in the Vehicle Registration Number, Doc Ref ID and Postcode fields are displayed
  Examples:
    | VehicleRegistrationNumber | DocRefID      | Postcode  |
    | "1XCG456"                 | "abgdrt12345" | "SA000AS" |

  Scenario Outline: Vehicle Not Found
    Given I visit assign web portal
    When I enter data in the <VehicleRegistrationNumber>,<DocRefID> and <Postcode> that does not match a valid vehicle record
    Then the vehicle not found page is displayed
  Examples:
    | VehicleRegistrationNumber | DocRefID      | Postcode |
    | "C1"                      | "11111111111" | "SA11AA" |

