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
    | origin                                                                     | target                                          | expected                                        | filled       | wiped       |
    | "setup-business-details"                                                   | "vehicle-lookup"                                | "vehicle-lookup"                                | "filled"     | "not wiped" |
    | "business-choose-your-address"                                             | "vehicle-lookup"                                | "vehicle-lookup"                                | "filled"     | "not wiped" |
    | "business-choose-your-address"                                             | "setup-business-details"                        | "setup-business-details"                        | "filled"     | "not wiped" |
    | "enter-address-manually"                                                   | "vehicle-lookup"                                | "vehicle-lookup"                                | "filled"     | "not wiped" |
    | "enter-address-manually"                                                   | "setup-business-details"                        | "setup-business-details"                        | "filled"     | "not wiped" |
    | "enter-address-manually"                                                   | "business-choose-your-address"                  | "business-choose-your-address"                  | "not filled" | "not wiped" |
    | "confirm-business"                                                         | "vehicle-lookup"                                | "vehicle-lookup"                                | "filled"     | "not wiped" |
    | "confirm-business"                                                         | "setup-business-details"                        | "setup-business-details"                        | "filled"     | "not wiped" |
    | "confirm-business"                                                         | "business-choose-your-address"                  | "business-choose-your-address"                  | "filled"     | "not wiped" |
    | "confirm-business"                                                         | "enter-address-manually"                        | "enter-address-manually"                        | "not filled" | "not wiped" |
    | "confirm-business (entered address manually)"                              | "enter-address-manually"                        | "enter-address-manually"                        | "filled"     | "not wiped" |
    | "capture-certificate-details (business acting)"                            | "setup-business-details"                        | "setup-business-details"                        | "filled"     | "not wiped" |
    | "capture-certificate-details (business acting)"                            | "business-choose-your-address"                  | "business-choose-your-address"                  | "filled"     | "not wiped" |
    | "capture-certificate-details (business acting) (entered address manually)" | "enter-address-manually"                        | "enter-address-manually"                        | "filled"     | "not wiped" |
    | "confirm (business acting)"                                                | "vehicle-lookup"                                | "vehicle-lookup"                                | "filled"     | "not wiped" |
    | "confirm (business acting)"                                                | "setup-business-details"                        | "setup-business-details"                        | "filled"     | "not wiped" |
    | "confirm (business acting)"                                                | "business-choose-your-address"                  | "business-choose-your-address"                  | "filled"     | "not wiped" |
    | "confirm (business acting) (entered address manually)"                     | "enter-address-manually"                        | "enter-address-manually"                        | "filled"     | "not wiped" |
    | "confirm (business acting)"                                                | "confirm-business"                              | "confirm-business"                              | "filled"     | "not wiped" |
    | "confirm (business acting)"                                                | "capture-certificate-details (business acting)" | "capture-certificate-details (business acting)" | "filled"     | "not wiped" |
    | "payment (business acting)"                                                | "vehicle-lookup"                                | "vehicle-lookup"                                | "filled"     | "not wiped" |
    | "payment (business acting)"                                                | "setup-business-details"                        | "setup-business-details"                        | "filled"     | "not wiped" |
    | "payment (business acting)"                                                | "business-choose-your-address"                  | "business-choose-your-address"                  | "filled"     | "not wiped" |
    | "payment (business acting)"                                                | "enter-address-manually"                        | "enter-address-manually"                        | "not filled" | "not wiped" |
    | "payment (business acting) (entered address manually)"                     | "enter-address-manually"                        | "enter-address-manually"                        | "filled"     | "not wiped" |
    | "payment (business acting)"                                                | "confirm-business"                              | "confirm-business"                              | "filled"     | "not wiped" |
    | "payment (business acting)"                                                | "capture-certificate-details (business acting)" | "capture-certificate-details (business acting)" | "filled"     | "not wiped" |
    | "payment (business acting)"                                                | "confirm (business acting)"                     | "confirm (business acting)"                     | "filled"     | "not wiped" |

  Scenario Outline: Pressing the browser's back button (keeper acting)
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

  Scenario Outline: Entering a url that is after the origin page (business acting)
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
    | origin                         | target                         | expected                       | filled       | wiped       |
#    | "setup-business-details"       | "business-choose-your-address" | "setup-business-details"       | "not filled" | "not wiped" |
#    | "setup-business-details"       | "enter-address-manually"       | "setup-business-details"       | "not filled" | "not wiped" |
#    | "setup-business-details"       | "confirm-business"             | "setup-business-details"       | "not filled" | "not wiped" | # TODO missing cookie check for confirm-business
#    | "setup-business-details"       | "capture-certificate-details"  | "setup-business-details"       | "not filled" | "not wiped" | # TODO missing cookie check for confirm-business
#    | "setup-business-details"       | "confirm"                      | "setup-business-details"       | "not filled" | "not wiped" | # TODO redirect is incorrect, goes to vehicle-lookup instead
#    | "setup-business-details"       | "payment"                      | "setup-business-details"       | "not filled" | "not wiped" | # TODO redirect is incorrect, goes to before-you-start instead
#    | "setup-business-details"       | "success"                      | "setup-business-details"       | "not filled" | "not wiped" | # TODO redirect is incorrect, goes to error page instead
#    | "business-choose-your-address" | "enter-address-manually"       | "enter-address-manually"       | "not filled" | "not wiped" |
#    | "business-choose-your-address" | "confirm-business"             | "business-choose-your-address" | "not filled" | "not wiped" | # TODO missing cookie check
#    | "business-choose-your-address" | "capture-certificate-details"  | "business-choose-your-address" | "not filled" | "not wiped" | # TODO missing cookie check
#    | "business-choose-your-address" | "confirm"                      | "business-choose-your-address" | "not filled" | "not wiped" | # TODO redirect is incorrect, goes to v lookup page instead
#    | "business-choose-your-address" | "payment"                      | "business-choose-your-address" | "not filled" | "not wiped" | # TODO redirect is incorrect, goes to before you start page instead
#    | "business-choose-your-address" | "success"                      | "business-choose-your-address" | "not filled" | "not wiped" | # TODO redirect is incorrect, goes to error page instead
#    | "enter-address-manually"       | "confirm-business"             | "business-choose-your-address" | "not filled" | "not wiped" | # TODO missing cookie check
#    | "enter-address-manually"       | "capture-certificate-details"  | "business-choose-your-address" | "not filled" | "not wiped" | # TODO missing cookie check
#    | "enter-address-manually"       | "confirm"                      | "business-choose-your-address" | "not filled" | "not wiped" | # TODO redirect is incorrect, goes to v lookup page instead
#    | "enter-address-manually"       | "confirm"                      | "payment"                      | "not filled" | "not wiped" | # TODO redirect is incorrect, goes to v lookup page instead
#    | "enter-address-manually"       | "confirm"                      | "success"                      | "not filled" | "not wiped" | # TODO redirect is incorrect, goes to v lookup page instead
#    | "confirm-business"             | "confirm"                      | "confirm-business"             | "not filled" | "not wiped" | # TODO redirect is incorrect, goes to v lookup page instead
#    | "confirm-business"             | "capture-certificate-details"  | "confirm-business"             | "not filled" | "not wiped" | # TODO missing cookie check
#    | "confirm-business"             | "payment"                      | "confirm-business"             | "not filled" | "not wiped" | # TODO redirect is incorrect, goes to before you start page instead
#    | "confirm-business"             | "success"                      | "confirm-business"             | "not filled" | "not wiped" | # TODO redirect is incorrect, goes to error page instead

  Scenario Outline: Pressing the browser's back button (business acting)
    Given that I am on the <origin> page
    When I press the browser's back button
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
    | origin                                          | expected                       | filled       | wiped   |
    | "setup-business-details"                        | "vehicle-lookup"               | "filled"     | "wiped" |
    | "business-choose-your-address"                  | "setup-business-details"       | "filled"     | "wiped" |
    | "enter-address-manually"                        | "business-choose-your-address" | "not filled" | "wiped" |
    | "confirm-business (entered address manually)"   | "enter-address-manually"       | "filled"     | "wiped" |
    | "confirm-business"                              | "business-choose-your-address" | "filled"     | "wiped" |
    | "capture-certificate-details (business acting)" | "confirm-business"             | "filled"     | "wiped" |