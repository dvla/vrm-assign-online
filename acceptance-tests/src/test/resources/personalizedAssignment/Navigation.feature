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
    | origin                                        | target                        | expected                      | filled       |
    | "vehicle-lookup"                              | "capture-certificate-details" | "vehicle-lookup"              | "not filled" |
    | "vehicle-lookup"                              | "confirm"                     | "vehicle-lookup"              | "not filled" |
    | "vehicle-lookup"                              | "payment"                     | "vehicle-lookup"              | "not filled" |
    | "vehicle-lookup"                              | "success"                     | "vehicle-lookup"              | "not filled" |
    | "capture-certificate-details (keeper acting)" | "confirm"                     | "capture-certificate-details" | "not filled" |
    | "capture-certificate-details (keeper acting)" | "payment"                     | "capture-certificate-details" | "not filled" |
    | "capture-certificate-details (keeper acting)" | "success"                     | "capture-certificate-details" | "not filled" |
    | "confirm"                                     | "payment"                     | "confirm"                     | "not filled" |
    | "confirm"                                     | "success"                     | "confirm"                     | "not filled" |
    #An odd case, we won't have a usable referrer in the header so cannot go back to the payment page.
    | "payment (keeper acting)"                     | "success"                     | "confirm"                     | "filled"     |

  Scenario Outline: Entering a url that is before the origin page (business acting)
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
  Examples:
    | origin                                                                     | target                         | expected                       | filled       |
    | "setup-business-details"                                                   | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     |
    | "confirm-business"                                                         | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     |
    | "confirm-business"                                                         | "setup-business-details"       | "setup-business-details"       | "filled"     |
    | "capture-certificate-details (business acting)"                            | "setup-business-details"       | "setup-business-details"       | "filled"     |
    | "confirm (business acting)"                                                | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     |
    | "confirm (business acting)"                                                | "setup-business-details"       | "setup-business-details"       | "filled"     |
    | "confirm (business acting)"                                                | "confirm-business"             | "confirm-business"             | "filled"     |
    | "confirm (business acting)"                                                | "capture-certificate-details"  | "capture-certificate-details"  | "filled"     |
    | "payment (business acting)"                                                | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     |
    | "payment (business acting)"                                                | "setup-business-details"       | "setup-business-details"       | "filled"     |
    | "payment (business acting)"                                                | "confirm-business"             | "confirm-business"             | "filled"     |
    | "payment (business acting)"                                                | "capture-certificate-details"  | "capture-certificate-details"  | "filled"     |
    | "payment (business acting)"                                                | "confirm"    | "confirm"    | "filled"     |

  Scenario Outline: Pressing the browser's back button back to the start page
    Given that I am on the <origin> page
    When I press the browser's back button
    Then I am redirected to the <expected> page
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
    | origin           | expected           | wiped   |
    | "vehicle-lookup" | "before-you-start" | "wiped" |

  Scenario Outline: Pressing the browser's back button (keeper acting)
    Given that I am on the <origin> page
    When I press the browser's back button
    Then I am redirected to the <expected> page
    And the <expected> form is filled with the values I previously entered
  Examples:
    | origin                                        | expected                      |
    | "capture-certificate-details (keeper acting)" | "vehicle-lookup"              |
    | "confirm"                                     | "capture-certificate-details" |
    | "payment (keeper acting)"                     | "confirm"                     |

  Scenario Outline: Entering a url that is after the origin page (business acting)
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is not filled with the values I previously entered
  Examples:
    | origin                         | target                         | expected                       |
    | "setup-business-details"       | "confirm-business"             | "setup-business-details"       |
    | "setup-business-details"       | "capture-certificate-details"  | "setup-business-details"       |
    | "setup-business-details"       | "confirm"                      | "setup-business-details"       |
    | "setup-business-details"       | "payment"                      | "setup-business-details"       |
    | "setup-business-details"       | "success"                      | "setup-business-details"       |
    | "confirm-business"             | "capture-certificate-details"  | "capture-certificate-details"  |
    | "confirm-business"             | "confirm"                      | "capture-certificate-details"  |
    | "confirm-business"             | "payment"                      | "capture-certificate-details"  |
    | "confirm-business"             | "success"                      | "capture-certificate-details"  |

  Scenario Outline: Pressing the browser's back button (business acting)
    Given that I am on the <origin> page
    When I press the browser's back button
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
  Examples:
    | origin                                          | expected                       | filled       |
    | "setup-business-details"                        | "vehicle-lookup"               | "filled"     |
    | "confirm-business"                              | "setup-business-details"       | "filled"     |
    | "capture-certificate-details (business acting)" | "confirm-business"             | "filled"     |