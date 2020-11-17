Feature: SSL and user session work correctly
  Scenario: Unable to view the portfolio page without logging in
    Given I enter the portfolio page without logging in
    Then I should be redirected to the login page
  
  Scenario: Without logging in, the index page redirects the user to the login page
    Given I access the index page
    Then I should be redirected to the login page

  Scenario: After logging in, the index page redirects the user to the portfolio page
    Given I logged in to the portfolio page
    When I wait for 1 second
    And I access the index page
    Then I should be redirected to the portfolio page

  Scenario: The log out button redirects the user to the login page
    Given I logged in to the portfolio page
    When I click the 'Log out' button
    Then I should be redirected to the login page
  
  Scenario: After logging out, the user session is cleared
    Given I logged in to the portfolio page
    When I click the 'Log out' button
    And I wait for 1 second
    And I access the index page
    Then I should be redirected to the login page
  
  Scenario: Access is denied if not connecting using HTTPS
    Given I access the index page using http
    Then The connection should be refused
  
  Scenario: User account is locked after 3 unsuccessful logging attempts
    Given I am on the login page
    And The account 'lunjohnzhang' with password 'cs310teamp' already exists
    When I type 'lunjohnzhang' to the 'Username' text input
    And I type '123' to the 'Password' text input
    And I click the 'Login' button
    And I wait for 1 second
    And I click the 'Login' button
    And I wait for 1 second
    And I click the 'Login' button
    And I wait for 1 second
    And I click the 'Login' button
    Then I should be notified 'You have made 3 failed login attempts in the past minute. Please try again a minute later.' below the 'Username' text input
  
  Scenario: Redirect back to the login page after session expires
    Given I logged in to the portfolio page
    When I wait for 120 seconds
    Then I should be redirected to the login page