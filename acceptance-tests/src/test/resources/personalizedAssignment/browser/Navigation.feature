Feature: Navigation

  Background:
    Given that I have started the PR Assign Service

  @WIP
  Scenario Outline: Entering a url that is ahead of the origin page
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
    | origin                                        | target                        | expected                      | filled   | wiped       |
#    | "vehicle-lookup"                              | "capture-certificate-details" | "vehicle-lookup"              | "filled" | "not wiped" | # TODO
#    | "vehicle-lookup" | "confirm" | "vehicle-lookup" | "filled" | "not wiped" |  # TODO
#    | "vehicle-lookup"                              | "payment"                     | "vehicle-lookup"              | "filled" | "not wiped" |  # TODO
#    | "vehicle-lookup"                              | "success"                     | "vehicle-lookup"              | "filled" | "not wiped" |  # TODO
#    | "capture-certificate-details (keeper acting)" | "confirm"                     | "capture-certificate-details" | "filled" | "not wiped" |  # TODO
#    | "capture-certificate-details (keeper acting)" | "payment"                     | "capture-certificate-details" | "filled" | "not wiped" |  # TODO
#    | "capture-certificate-details (keeper acting)" | "success"                     | "capture-certificate-details" | "filled" | "not wiped" |  # TODO
#    | "confirm"                                     | "payment"                     | "confirm"                     | "filled" | "not wiped" |  # TODO
#    | "confirm"                                     | "success"                     | "confirm"                     | "filled" | "not wiped" |  # TODO
#    | "payment (keeper acting)"                                     | "success"                     | "payment"                     | "_"      | "not wiped" |  # TODO


  @WIP
  Scenario Outline: Pressing the browser's back button
    Given that I am on the <origin> page
    When I press the browser's back button
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
    | origin                                        | expected                      | filled   | wiped       |
    | "vehicle-lookup"                              | "before-you-start"            | "-"      | "wiped"     |
    | "capture-certificate-details (keeper acting)" | "vehicle-lookup"              | "filled" | "not wiped" |
    | "confirm"                                     | "capture-certificate-details" | "filled" | "not wiped" |
    | "payment (keeper acting)"                     | "confirm"                     | "filled" | "not wiped" |
    | "success"                                     | "payment-prevent-back"        | "-"      | "wiped"     |