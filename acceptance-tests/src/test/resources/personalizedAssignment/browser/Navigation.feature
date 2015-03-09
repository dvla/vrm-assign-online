Feature: Navigation

  Background:
    Given that I have started the PR Assign Service

  Scenario Outline: Entering a url that is before the origin page (keeper acting)
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
    | origin                                        | target                        | expected                      | filled   | wiped       |
#    | "success" | "vehicle-lookup"              | "vehicle-lookup" | "not filled" | "wiped" | # TODO filled
#    | "success" | "capture-certificate-details" | "vehicle-lookup" | "not filled" | "wiped" | # TODO filled
#    | "success" | "confirm"              | "vehicle-lookup" | "not filled" | "wiped" | # TODO filled
#    | "success" | "payment"              | "vehicle-lookup" | "not filled" | "wiped" | # TODO filled

  Scenario Outline: Entering a url that is before the origin page (business acting)
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
    | origin                                                                     | target                         | expected                       | filled       | wiped       |
#  |"success"	|"vehicle-lookup" |	"vehicle-lookup" 	| "not filled" | "wiped" |
#  |"success"	|"setup-business-details"	|"vehicle-lookup" 	| "not filled" | "wiped" |
#  |"success"	|"business-choose-your-address"|	"vehicle-lookup" 	| "not filled" | "wiped" |
#  |"success"	|"enter-address-manually"|	"vehicle-lookup" 	| "not filled" | "wiped" |
#  |"success"	|"confirm-business"|	"vehicle-lookup" 	| "not filled" | "wiped" |
#  |"success"	|"capture-certificate-details (business acting)"	|"vehicle-lookup" 	| "not filled" | "wiped" |
#  |"success"	|"confirm (business acting)"|	"vehicle-lookup" 	| "not filled" | "wiped" |
#  |"success"	|"payment (business acting)"	|"vehicle-lookup" 	| "not filled" | "wiped" |

  @WIP
  Scenario Outline: Entering a url that is after the origin page
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
    | origin | target | expected | filled | wiped |
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

  Scenario Outline: Pressing the browser's back button
    Given that I am on the <origin> page
    When I press the browser's back button
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
    | origin                                        | expected                      | filled   | wiped       |
#    | "success"                                     | "payment-prevent-back"        | "-"      | "wiped"     |