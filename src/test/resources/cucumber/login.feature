Feature: Login to account
  Scenario: Able to see all the elements on login page
    Given I am on the login page
    Then I should see a 'Login' button on the login page
    And I should see a 'Create Account' button on the login page
    And I should see a text input named 'Username'
    And I should see a text input named 'Password'

  Scenario: Able to see all the elements on login page in mobile view
    Given I am using the mobile view
    And I am on the login page
    Then I should be able to click the 'Login' button
    And I should be able to click the 'Create Account' button
    And I should be able to click the 'Username' text input
    And I should be able to click the 'Password' text input

  Scenario: Redirect to the register page
    Given I am on the login page
    And I click the 'Create Account' button
    Then I should be redirected to the register page
    
  Scenario: Do not allow login with nonexistent username
    Given I am on the login page
    And the account with username '$findstr' doesn't exist yet
    When I type '$findstr' to the 'Username' text input
    And I type 'cs310teamp' to the 'Password' text input
    And I click the 'Login' button
    Then I should be notified 'No account was found.' below the 'Username' text input

  Scenario: Do not allow login with incorrect password
    Given I am on the login page
    And The account 'findstr' with password 'cs310teamp' already exists
    When I type 'findstr' to the 'Username' text input
    And I type '123' to the 'Password' text input
    And I click the 'Login' button
    Then I should be notified 'Invalid password.' below the 'Password' text input

  Scenario: Login successfully
    Given I am on the login page
    And The account 'findstr3' with password 'cs310teamp' already exists
    When I type 'findstr3' to the 'Username' text input
    And I type 'cs310teamp' to the 'Password' text input
    And I click the 'Login' button
    Then I should be redirected to the portfolio page

