Feature: Register a vehicle and confirm
  Scenario: Register a vehicle
    Given I am logged in as "michelle@gmail.com"
    When I type in "KYJ113", "ma", "PETROL", "2001-02-03","TOYOTA","WISH"
    Then I should be able to see vehicle with plate "KYJ113" is registered 
    And I should be able to get "TOYOTA WISH of plate KYJ113 is successfully registered."
 
 Scenario: Register a vehicle which is in database and if in, return true
    Given "KYJ113" is in the WOF database
    When I try to register "KYJ113", "ma", "PETROL", "2001-02-03","TOYOTA","WISH"
    Then I should find vehicle already exsists is "true"
