Feature: Register an owner and confirm
  Scenario: Register an owner who is not in database and if not in return firstname
    Given I am connected to the WOF database
    When I enter "bob", "james", "bob@gmail.com", "1234567"
    Then I should see "bob@gmail.com" 
    And I should get "bob james is successfully registered.Now you can register your vehicle."
 
 Scenario: Register an owner who is in database and if in, return true
    Given "bob@gmail.com" is already in the WOF database
    When I submit "bob", "james", "bob@gmail.com", "1234567"
    Then I should find "true"

  
