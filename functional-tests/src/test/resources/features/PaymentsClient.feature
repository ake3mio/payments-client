Feature: Payments Client

  Scenario: The user makes payments to several recipients
    Given the user has a set of recipients to pay
      | ID       | Recipient   | Amount | Currency |
      | aaaaaaaa | 12125551000 | 10     | KES      |
      | bbbbbbbb | 12125550000 | 50     | USD      |
      | xyz12345 | 12125551004 | 150.35 | USD      |
      | 29387431 | 12125550003 | 37     | NGN      |
    When the payments are submitted to the client
    Then a report is generated showing all succeeded transactions

  Scenario: The user makes payments to several recipients and all transactions fail
    Given the user has a set of recipients to pay
      | ID       | Recipient   | Amount | Currency |
      | aaaaaaaa | 12125551000 | 10     | KES      |
      | bbbbbbbb | 12125550000 | 50     | USD      |
      | xyz12345 | 12125551004 | 150.35 | USD      |
      | 29387431 | 12125550003 | 37     | NGN      |
    When the payments are submitted to the client
    Then a report is generated showing all failed transactions
