Feature: Navigation

  Background:
    Given that I have started the PR Assign Service

  Scenario Outline: Entering a url that is before the origin page (keeper acting)
    Given that I am on the <origin> page with <vehicle-registration-number>
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered <vehicle-registration-number>
  Examples:
    | origin                                        | vehicle-registration-number | target                        | expected                      | filled   |
    | "capture-certificate-details (keeper acting)" | "A2"                        | "vehicle-lookup"              | "vehicle-lookup"              | "filled" |
    | "confirm"                                     | "A3"                        | "vehicle-lookup"              | "vehicle-lookup"              | "filled" |
    | "confirm"                                     | "A4"                        | "capture-certificate-details" | "capture-certificate-details" | "filled" |
    | "payment (keeper acting)"                     | "DD22"                      | "vehicle-lookup"              | "vehicle-lookup"              | "filled" |
    | "payment (keeper acting)"                     | "DD22"                      | "capture-certificate-details" | "capture-certificate-details" | "filled" |
    | "payment (keeper acting)"                     | "DD22"                      | "confirm"                     | "confirm"                     | "filled" |

  Scenario Outline: Entering a url that is after the origin page
    Given that I am on the <origin> page with <vehicle-registration-number>
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered <vehicle-registration-number>
  Examples:
    | origin                                        | vehicle-registration-number | target                        | expected                      | filled       |
    | "vehicle-lookup"                              | "A2"                        | "capture-certificate-details" | "vehicle-lookup"              | "not filled" |
    | "vehicle-lookup"                              | "A3"                        | "confirm"                     | "vehicle-lookup"              | "not filled" |
    | "vehicle-lookup"                              | "DD22"                      | "payment"                     | "vehicle-lookup"              | "not filled" |
    | "vehicle-lookup"                              | "A5"                        | "success"                     | "vehicle-lookup"              | "not filled" |
    | "capture-certificate-details (keeper acting)" | "A6"                        | "confirm"                     | "capture-certificate-details" | "not filled" |
    | "capture-certificate-details (keeper acting)" | "DD22"                      | "payment"                     | "capture-certificate-details" | "not filled" |
    | "capture-certificate-details (keeper acting)" | "A8"                        | "success"                     | "capture-certificate-details" | "not filled" |
    | "confirm"                                     | "DD22"                      | "payment"                     | "confirm"                     | "not filled" |
    | "confirm"                                     | "A9"                        | "success"                     | "confirm"                     | "not filled" |
    #An odd case, we won't have a usable referrer in the header so cannot go back to the payment page.
    | "payment (keeper acting)"                     | "DD22"                      | "success"                     | "confirm"                     | "filled"     |

  Scenario Outline: Entering a url that is before the origin page (business acting)
    Given that I am on the <origin> page with <vehicle-registration-number>
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered <vehicle-registration-number>
  Examples:
    | origin                                          | vehicle-registration-number | target                         | expected                       | filled       |
    | "setup-business-details"                        | "A2"                        | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     |
    | "confirm-business"                              | "A3"                        | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     |
    | "confirm-business"                              | "A4"                        | "setup-business-details"       | "setup-business-details"       | "filled"     |
    | "capture-certificate-details (business acting)" | "A5"                        | "setup-business-details"       | "setup-business-details"       | "filled"     |
    | "confirm (business acting)"                     | "A6"                        | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     |
    | "confirm (business acting)"                     | "A7"                        | "setup-business-details"       | "setup-business-details"       | "filled"     |
    | "confirm (business acting)"                     | "A8"                        | "confirm-business"             | "confirm-business"             | "filled"     |
    | "confirm (business acting)"                     | "A9"                        | "capture-certificate-details"  | "capture-certificate-details"  | "filled"     |
    | "payment (business acting)"                     | "DD22"                      | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     |
    | "payment (business acting)"                     | "DD22"                      | "setup-business-details"       | "setup-business-details"       | "filled"     |
    | "payment (business acting)"                     | "DD22"                      | "confirm-business"             | "confirm-business"             | "filled"     |
    | "payment (business acting)"                     | "DD22"                      | "capture-certificate-details"  | "capture-certificate-details"  | "filled"     |
    | "payment (business acting)"                     | "DD22"                      | "confirm"                      | "confirm"                      | "filled"     |

  Scenario Outline: Pressing the browser's back button back to the start page
    Given that I am on the <origin> page with <vehicle-registration-number>
    When I press the browser's back button
    Then I am redirected to the <expected> page
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
    | origin           | vehicle-registration-number | expected           | wiped   |
    | "vehicle-lookup" | "A2"                        | "before-you-start" | "wiped" |

  Scenario Outline: Pressing the browser's back button (keeper acting)
    Given that I am on the <origin> page with <vehicle-registration-number>
    When I press the browser's back button
    Then I am redirected to the <expected> page
    And the <expected> form is filled with the values I previously entered <vehicle-registration-number>
  Examples:
    | origin                                        | vehicle-registration-number | expected                      |
    | "capture-certificate-details (keeper acting)" | "A2"                        | "vehicle-lookup"              |
    | "confirm"                                     | "A3"                        | "capture-certificate-details" |
    | "payment (keeper acting)"                     | "DD22"                      | "confirm-payment"             |

  Scenario Outline: Entering a url that is after the origin page (business acting)
    Given that I am on the <origin> page with <vehicle-registration-number>
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is not filled with the values I previously entered
  Examples:
    | origin                         | vehicle-registration-number | target                         | expected                       |
    | "setup-business-details"       | "A2"                        | "confirm-business"             | "setup-business-details"       |
    | "setup-business-details"       | "A3"                        | "capture-certificate-details"  | "setup-business-details"       |
    | "setup-business-details"       | "A4"                        | "confirm"                      | "setup-business-details"       |
    | "setup-business-details"       | "DD22"                      | "payment"                      | "setup-business-details"       |
    | "setup-business-details"       | "A5"                        | "success"                      | "setup-business-details"       |
    | "confirm-business"             | "A6"                        | "capture-certificate-details"  | "capture-certificate-details"  |
    | "confirm-business"             | "A7"                        | "confirm"                      | "capture-certificate-details"  |
    | "confirm-business"             | "DD22"                      | "payment"                      | "capture-certificate-details"  |
    | "confirm-business"             | "A8"                        | "success"                      | "capture-certificate-details"  |

  Scenario Outline: Pressing the browser's back button (business acting)
    Given that I am on the <origin> page with <vehicle-registration-number>
    When I press the browser's back button
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered <vehicle-registration-number>
  Examples:
    | origin                                          | vehicle-registration-number | expected                       | filled       |
    | "setup-business-details"                        | "A2"                        | "vehicle-lookup"               | "filled"     |
    | "confirm-business"                              | "A3"                        | "setup-business-details"       | "filled"     |
    | "capture-certificate-details (business acting)" | "A4"                        | "confirm-business"             | "filled"     |