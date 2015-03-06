@WIP
Feature: Navigation

  Background:
    Given that I have started the PR Assign Service

  Scenario Outline: Entering a url that is ahead of the origin page
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
    | origin                                        | target           | expected         | filled   | wiped       |
#    | "vehicle-lookup" | "confirm" | "vehicle-lookup" | "filled" | "not wiped" |
    | "capture-certificate-details (keeper acting)" | "vehicle-lookup" | "vehicle-lookup" | "filled" | "not wiped" |

#  confirm	vehicle-lookup 	vehicle-lookup 	Y	N
#  confirm	capture-certificate-details	capture-certificate-details	Y	N
#  payment	vehicle-lookup 	vehicle-lookup 	Y	N
#  payment	capture-certificate-details	capture-certificate-details
#  payment	confirm	confirm	Y	N
#  success	vehicle-lookup 	vehicle-lookup 	N	Y
#  success	capture-certificate-details	vehicle-lookup 	N	Y
#  success	confirm	vehicle-lookup 	N	Y
#  success	payment	vehicle-lookup 	N	Y


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