Feature: Create an account
  Scenario: Able to see all the elements on register page
    Given I am on the register page
    Then I should see a 'Cancel' button on the register page
    And I should see a 'Create User' button on the register page
    And I should see a text input named 'Username'
    And I should see a text input named 'Password'
    And I should see a text input named 'Repeat Password'

  Scenario: Able to see all the elements on register page in mobile view
    Given I am using the mobile view
    And I am on the register page
    Then I should be able to click the 'Cancel' button
    And I should be able to click the 'Create User' button
    And I should be able to click the 'Username' text input
    And I should be able to click the 'Password' text input
    And I should be able to click the 'Repeat Password' text input

  Scenario: Do not allow creating account with malformed username
    Given I am on the register page
    And the account with username '$findstr' doesn't exist yet
    When I type '$findstr' to the 'Username' text input
    And I type 'cs310teamp' to the 'Password' text input
    And I type 'cs310teamp' to the 'Repeat Password' text input
    And I click the 'Create User' button
    Then I should be notified 'Username should be between 3 and 20 characters and only contain alphanumeric characters or underscore' below the 'Username' text input
    
  Scenario: Do not allow creating account with malformed password
    Given I am on the register page
    And the account with username 'findstr' doesn't exist yet
    When I type 'findstr' to the 'Username' text input
    And I type '123' to the 'Password' text input
    And I type '123' to the 'Repeat Password' text input
    And I click the 'Create User' button
    Then I should be notified 'Password should be between 6 and 20 characters' below the 'Password' text input
    
  Scenario: Do not allow creating account without repeating password
    Given I am on the register page
    And the account with username 'findstr' doesn't exist yet
    When I type 'findstr' to the 'Username' text input
    And I type 'cs310teamp' to the 'Password' text input
    And I type '' to the 'Repeat Password' text input
    And I click the 'Create User' button
    Then I should be notified 'The password does not match' below the 'Repeat Password' text input
    
  Scenario: Do not allow creating account with unmatched passwords
    Given I am on the register page
    And the account with username 'findstr' doesn't exist yet
    When I type 'findstr' to the 'Username' text input
    And I type 'cs310teamp' to the 'Password' text input
    And I type 'cs201teamp' to the 'Repeat Password' text input
    And I click the 'Create User' button
    Then I should be notified 'The password does not match' below the 'Repeat Password' text input
    
  Scenario: Do not allow creating account with duplicate username
    Given I am on the register page
    And The account 'findstr2' with password 'cs310teamp' already exists
    When I type 'findstr2' to the 'Username' text input
    And I type 'cs310teamp' to the 'Password' text input
    And I type 'cs310teamp' to the 'Repeat Password' text input
    And I click the 'Create User' button
    Then I should be notified 'An account associated with that username already exists' below the 'Username' text input

  Scenario: Create account successfully
    Given I am on the register page
    And the account with username 'findstr' doesn't exist yet
    When I type 'findstr' to the 'Username' text input
    And I type 'cs310teamp' to the 'Password' text input
    And I type 'cs310teamp' to the 'Repeat Password' text input
    And I click the 'Create User' button
    Then I should be redirected to the login page

  Scenario: Cancel and return to the login page
    Given I am on the register page
    And I click the 'Cancel' button
    Then I should be redirected to the login page