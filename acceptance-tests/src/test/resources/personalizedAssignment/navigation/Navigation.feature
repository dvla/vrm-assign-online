#@WIP
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
    | origin           | target    | expected         | filled   | wiped       |
    | "vehicle-lookup" | "confirm" | "vehicle-lookup" | "filled" | "not wiped" |
#    | "vehicle-lookup" | "payment" | "vehicle-lookup" | "filled" | "not wiped" |
#    | "vehicle-lookup" | "success" | "vehicle-lookup" | "filled" | "not wiped" |
#    | "confirm"        | "payment" | "confirm"        | "filled" | "not wiped" |
#    | "confirm"        | "success" | "confirm"        | "filled" | "not wiped" |
#    | "payment"        | "success" | "payment"        | "-"      | "not wiped" |
#    | "payment"        | "success" | "payment"        | "-"      | "not wiped" |


  Scenario Outline: Pressing the browser's back button
    Given that I am on the <origin> page
    When I press the browser's back button
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
  | origin           |  expected         | filled   | wiped       |
  | "vehicle-lookup" | "before-you-start" | "-" | "wiped" |

#  on page	result	form filled	wipe cookies (payment, retain, both v&k)
#  vehicle-lookup 	before you start	-	Y
#  confirm	vehicle-lookup 	Y	N
#  payment	confirm	Y	N
#  success	payment prevent back	-	Y