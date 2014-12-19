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