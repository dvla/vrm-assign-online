Feature: Navigation

  Background:
    Given that I have started the PR Assign Service

  Scenario Outline: Entering a url that is before the origin page (keeper acting)
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
  Examples:
    | origin                                        | target                        | expected                      | filled   |
    | "capture-certificate-details (keeper acting)" | "vehicle-lookup"              | "vehicle-lookup"              | "filled" |
    | "confirm"                                     | "vehicle-lookup"              | "vehicle-lookup"              | "filled" |
    | "confirm"                                     | "capture-certificate-details" | "capture-certificate-details" | "filled" |
    | "payment (keeper acting)"                     | "vehicle-lookup"              | "vehicle-lookup"              | "filled" |
    | "payment (keeper acting)"                     | "capture-certificate-details" | "capture-certificate-details" | "filled" |
    | "payment (keeper acting)"                     | "confirm"                     | "confirm"                     | "filled" |

  Scenario Outline: Entering a url that is after the origin page
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
  Examples:
    | origin           | target                        | expected         | filled       |
    | "vehicle-lookup" | "capture-certificate-details" | "vehicle-lookup" | "not filled" |
    | "vehicle-lookup" | "confirm"                     | "vehicle-lookup" | "not filled" |
#    | "vehicle-lookup"                              | "payment"                     | "vehicle-lookup"              | "not filled" | # TODO redirect is incorrect, goes to before-you-start instead
#    | "vehicle-lookup"                              | "success"                     | "vehicle-lookup"              | "not filled" | # TODO redirect is incorrect, goes to error page instead
#    | "capture-certificate-details (keeper acting)" | "confirm"                     | "capture-certificate-details" | "not filled" | # TODO redirect is incorrect, goes to vehicle lookup instead
#    | "capture-certificate-details (keeper acting)" | "payment"                     | "capture-certificate-details" | "not filled" | # TODO redirect is incorrect, goes to before-you-start instead
#    | "capture-certificate-details (keeper acting)" | "success"                     | "capture-certificate-details" | "not filled" | # TODO redirect is incorrect, goes to error page instead
#    | "confirm"                                     | "payment"                     | "confirm"                     | "not filled" | # TODO redirect is incorrect, goes to payment-failure page instead
#    | "confirm"                                     | "success"                     | "confirm"                     | "not filled" | # TODO redirect is incorrect, goes to error page instead
#    | "payment (keeper acting)"                     | "success"                     | "payment"                     | "_"          | # TODO redirect is incorrect, goes to error page instead

  Scenario Outline: Entering a url that is before the origin page (business acting)
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
  Examples:
    | origin                                                                     | target                                          | expected                                        | filled       |
    | "setup-business-details"                                                   | "vehicle-lookup"                                | "vehicle-lookup"                                | "filled"     |
    | "business-choose-your-address"                                             | "vehicle-lookup"                                | "vehicle-lookup"                                | "filled"     |
    | "business-choose-your-address"                                             | "setup-business-details"                        | "setup-business-details"                        | "filled"     |
    | "enter-address-manually"                                                   | "vehicle-lookup"                                | "vehicle-lookup"                                | "filled"     |
    | "enter-address-manually"                                                   | "setup-business-details"                        | "setup-business-details"                        | "filled"     |
    | "enter-address-manually"                                                   | "business-choose-your-address"                  | "business-choose-your-address"                  | "not filled" |
    | "confirm-business"                                                         | "vehicle-lookup"                                | "vehicle-lookup"                                | "filled"     |
    | "confirm-business"                                                         | "setup-business-details"                        | "setup-business-details"                        | "filled"     |
    | "confirm-business"                                                         | "business-choose-your-address"                  | "business-choose-your-address"                  | "filled"     |
    | "confirm-business"                                                         | "enter-address-manually"                        | "enter-address-manually"                        | "not filled" |
    | "confirm-business (entered address manually)"                              | "enter-address-manually"                        | "enter-address-manually"                        | "filled"     |
    | "capture-certificate-details (business acting)"                            | "setup-business-details"                        | "setup-business-details"                        | "filled"     |
    | "capture-certificate-details (business acting)"                            | "business-choose-your-address"                  | "business-choose-your-address"                  | "filled"     |
    | "capture-certificate-details (business acting) (entered address manually)" | "enter-address-manually"                        | "enter-address-manually"                        | "filled"     |
    | "confirm (business acting)"                                                | "vehicle-lookup"                                | "vehicle-lookup"                                | "filled"     |
    | "confirm (business acting)"                                                | "setup-business-details"                        | "setup-business-details"                        | "filled"     |
    | "confirm (business acting)"                                                | "business-choose-your-address"                  | "business-choose-your-address"                  | "filled"     |
    | "confirm (business acting) (entered address manually)"                     | "enter-address-manually"                        | "enter-address-manually"                        | "filled"     |
    | "confirm (business acting)"                                                | "confirm-business"                              | "confirm-business"                              | "filled"     |
    | "confirm (business acting)"                                                | "capture-certificate-details (business acting)" | "capture-certificate-details (business acting)" | "filled"     |
    | "payment (business acting)"                                                | "vehicle-lookup"                                | "vehicle-lookup"                                | "filled"     |
    | "payment (business acting)"                                                | "setup-business-details"                        | "setup-business-details"                        | "filled"     |
    | "payment (business acting)"                                                | "business-choose-your-address"                  | "business-choose-your-address"                  | "filled"     |
    | "payment (business acting)"                                                | "enter-address-manually"                        | "enter-address-manually"                        | "not filled" |
    | "payment (business acting) (entered address manually)"                     | "enter-address-manually"                        | "enter-address-manually"                        | "filled"     |
    | "payment (business acting)"                                                | "confirm-business"                              | "confirm-business"                              | "filled"     |
    | "payment (business acting)"                                                | "capture-certificate-details (business acting)" | "capture-certificate-details (business acting)" | "filled"     |
    | "payment (business acting)"                                                | "confirm (business acting)"                     | "confirm (business acting)"                     | "filled"     |

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
  Examples:
    | origin | target | expected | filled |
    | "setup-business-details"       | "business-choose-your-address" | "setup-business-details"       | "not filled" |
    | "setup-business-details"       | "enter-address-manually"       | "setup-business-details"       | "not filled" |
#    | "setup-business-details"       | "confirm-business"             | "setup-business-details"       | "not filled" | # TODO missing cookie check for confirm-business
#    | "setup-business-details"       | "capture-certificate-details"  | "setup-business-details"       | "not filled" | # TODO missing cookie check for confirm-business
#    | "setup-business-details"       | "confirm"                      | "setup-business-details"       | "not filled" | # TODO redirect is incorrect, goes to vehicle-lookup instead
#    | "setup-business-details"       | "payment"                      | "setup-business-details"       | "not filled" | # TODO redirect is incorrect, goes to before-you-start instead
#    | "setup-business-details"       | "success"                      | "setup-business-details"       | "not filled" | # TODO redirect is incorrect, goes to error page instead
    | "business-choose-your-address" | "enter-address-manually"       | "enter-address-manually"       | "not filled" |
#    | "business-choose-your-address" | "confirm-business"             | "business-choose-your-address" | "not filled" | # TODO missing cookie check
#    | "business-choose-your-address" | "capture-certificate-details"  | "business-choose-your-address" | "not filled" | # TODO missing cookie check
#    | "business-choose-your-address" | "confirm"                      | "business-choose-your-address" | "not filled" | # TODO redirect is incorrect, goes to v lookup page instead
#    | "business-choose-your-address" | "payment"                      | "business-choose-your-address" | "not filled" | # TODO redirect is incorrect, goes to before you start page instead
#    | "business-choose-your-address" | "success"                      | "business-choose-your-address" | "not filled" | # TODO redirect is incorrect, goes to error page instead
#    | "enter-address-manually"       | "confirm-business"             | "business-choose-your-address" | "not filled" | # TODO missing cookie check
#    | "enter-address-manually"       | "capture-certificate-details"  | "business-choose-your-address" | "not filled" | # TODO missing cookie check
#    | "enter-address-manually"       | "confirm"                      | "business-choose-your-address" | "not filled" | # TODO redirect is incorrect, goes to v lookup page instead
#    | "enter-address-manually"       | "confirm"                      | "payment"                      | "not filled" | # TODO redirect is incorrect, goes to v lookup page instead
#    | "enter-address-manually"       | "confirm"                      | "success"                      | "not filled" | # TODO redirect is incorrect, goes to v lookup page instead
#    | "confirm-business"             | "confirm"                      | "confirm-business"             | "not filled" | # TODO redirect is incorrect, goes to v lookup page instead
#    | "confirm-business"             | "capture-certificate-details"  | "confirm-business"             | "not filled" | # TODO missing cookie check
#    | "confirm-business"             | "payment"                      | "confirm-business"             | "not filled" | # TODO redirect is incorrect, goes to before you start page instead
#    | "confirm-business"             | "success"                      | "confirm-business"             | "not filled" | # TODO redirect is incorrect, goes to error page instead

  Scenario Outline: Pressing the browser's back button (business acting)
    Given that I am on the <origin> page
    When I press the browser's back button
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
  Examples:
    | origin                                          | expected                       | filled       |
    | "setup-business-details"                        | "vehicle-lookup"               | "filled"     |
    | "business-choose-your-address"                  | "setup-business-details"       | "filled"     |
    | "enter-address-manually"                        | "business-choose-your-address" | "not filled" |
    | "confirm-business (entered address manually)"   | "enter-address-manually"       | "filled"     |
    | "confirm-business"                              | "business-choose-your-address" | "filled"     |
    | "capture-certificate-details (business acting)" | "confirm-business"             | "filled"     |