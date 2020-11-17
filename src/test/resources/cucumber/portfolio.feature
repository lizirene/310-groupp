Feature: Appearance of the portfolio page
  Scenario: Able to see all the elements on portfolio page
    Given I logged in to the portfolio page
    Then I should see a 'Add stock' button on the portfolio page
    And I should see a 'View stock' button on the portfolio page
    And I should see a 'Upload portfolio' button on the portfolio page
    And I should see a chart on the portfolio page
    And I should see a 'Log out' button in the app bar

  Scenario: Able to see all the elements on portfolio page in mobile view
    Given I am using the mobile view
    And I logged in to the portfolio page
    Then I should be able to click the 'Add stock' button
    And I should be able to click the 'View stock' button
    And I should be able to click the 'Upload portfolio' button
    And I should see a chart on the portfolio page
    And I should be able to click the 'Log out' button
    
  Scenario: Portfolio value of the last trading day increased
    Given I logged in to the portfolio page
    When I click the 'Add Stock' button
    And I type 'TSLA' to the 'Ticker' text input
    And I type '1' to the '# of shares' text input
    And I type the last trading day to the 'Date bought' text input
    And I click the 'Add Stock' button in the popup window
    Then The popup window should disappear
    And The stock 'TSLA' should show up in my portfolio stocks
    And My stock portfolio value should increase with color being green
    And The percentage value should be positive and be green
    And The arrow should be green and points upward

  Scenario: Portfolio value of the last trading day decreased
    Given I logged in to the portfolio page
    When I click the 'Add Stock' button
    And I type 'TSLA' to the 'Ticker' text input
    And I type '1' to the '# of shares' text input
    And I type the second to last trading day to the 'Date bought' text input
    And I type the last trading day to the 'Date sold' text input
    And I click the 'Add Stock' button in the popup window
    Then The popup window should disappear
    And The stock 'TSLA' should show up in my portfolio stocks
    And My stock portfolio value should decrease with color being red
    And The percentage value should be positive and be red
    And The arrow should be green and points downward