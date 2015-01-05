Feature: Registered Keeper

  Scenario Outline:
    Given User is on the start page
    When he clicks starts now
    And he enters <VehiclesRegistrationNumber> ,<DocRef> and <Postcode>
    Then certificate page is displayed

  Examples:
    | VehiclesRegistrationNumber | DocRef  | Postcode |
    | "test"                     | "tests" | "test67" |