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
    | "capture-certificate-details (keeper acting)" | "vehicle-lookup"              | "vehicle-lookup"              | "filled" | "not wiped" |
    | "confirm"                                     | "vehicle-lookup"              | "vehicle-lookup"              | "filled" | "not wiped" |
    | "confirm"                                     | "capture-certificate-details" | "capture-certificate-details" | "filled" | "not wiped" |
    | "payment (keeper acting)"                     | "vehicle-lookup"              | "vehicle-lookup"              | "filled" | "not wiped" |
    | "payment (keeper acting)"                     | "capture-certificate-details" | "capture-certificate-details" | "filled" | "not wiped" |
    | "payment (keeper acting)"                     | "confirm"                     | "confirm"                     | "filled" | "not wiped" |

  Scenario Outline: Entering a url that is before the origin page (business acting)
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
    | origin                                                                     | target                         | expected                       | filled       | wiped       |
    | "setup-business-details"                                                   | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     | "not wiped" |
    | "business-choose-your-address"                                             | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     | "not wiped" |
    | "business-choose-your-address"                                             | "setup-business-details"       | "setup-business-details"       | "filled"     | "not wiped" |
    | "enter-address-manually"                                                   | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     | "not wiped" |
    | "enter-address-manually"                                                   | "setup-business-details"       | "setup-business-details"       | "filled"     | "not wiped" |
    | "enter-address-manually"                                                   | "business-choose-your-address" | "business-choose-your-address" | "not filled" | "not wiped" |
    | "confirm-business"                                                         | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     | "not wiped" |
    | "confirm-business"                                                         | "setup-business-details"       | "setup-business-details"       | "filled"     | "not wiped" |
    | "confirm-business"                                                         | "business-choose-your-address" | "business-choose-your-address" | "filled"     | "not wiped" |
    | "confirm-business"                                                         | "enter-address-manually"       | "enter-address-manually"       | "not filled" | "not wiped" |
    | "confirm-business (entered address manually)"                              | "enter-address-manually"       | "enter-address-manually"       | "filled"     | "not wiped" |
    | "capture-certificate-details (business acting)"                            | "setup-business-details"       | "setup-business-details"       | "filled"     | "not wiped" |
    | "capture-certificate-details (business acting)"                            | "business-choose-your-address" | "business-choose-your-address" | "filled"     | "not wiped" |
    | "capture-certificate-details (business acting) (entered address manually)" | "enter-address-manually"       | "enter-address-manually"       | "filled"     | "not wiped" |
#  |"confirm (business acting)"|	"vehicle-lookup" |	"vehicle-lookup" 	| "filled" | "not wiped" |
#  |"confirm (business acting)"|	"setup-business-details"|	"setup-business-details"	| "filled" | "not wiped" |
#  |"confirm (business acting)"|	"business-choose-your-address"|	"business-choose-your-address"	| "filled" | "not wiped" |
#  |"confirm (business acting) (entered address manually)|	"enter-address-manually"|	"enter-address-manually"	| "filled" | "not wiped" |
#  |"confirm (business acting)"	|"confirm-business"|	"confirm-business"	| "filled" | "not wiped" |
#  |"confirm (business acting)"	|"capture-certificate-details (business acting)"	|"capture-certificate-details (business acting)"	| "filled" | "not wiped" |
#  |"payment (business acting)"|	"vehicle-lookup" 	|"vehicle-lookup" 	| "filled" | "not wiped" |
#  |"payment (business acting)"	|"setup-business-details"|	"setup-business-details"	| "filled" | "not wiped" |
#  |"payment (business acting)"|	"business-choose-your-address"|	"business-choose-your-address"	| "filled" | "not wiped" |
#  |"payment (business acting)"|	"enter-address-manually"	|"enter-address-manually"	| "filled" | "not wiped" |
#  |"payment (business acting)"|	"confirm-business"|	"confirm-business"	| "filled" | "not wiped" |
#  |"payment (business acting)"|	"capture-certificate-details (business acting)"|	"capture-certificate-details (business acting)"	| "filled" | "not wiped" |
#  |"payment (business acting)"|	"confirm (business acting)"|	"confirm (business acting)"	| "filled" | "not wiped" |


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