Feature: Add a historical stock for visualization and comparison
  Scenario: Able to see the View Stock button on Portfolio page
    Given I logged in to the portfolio page
    Then I should see a 'View Stock' button on the portfolio page

  Scenario: Able to see the view-historical-stock popup window
    Given I logged in to the portfolio page
    When I click the 'View Stock' button
    Then I should see a popup window titled 'View historical stock'
    And I should see a text input named 'Ticker'
    And I should see a text input named '# of shares'
    And I should see a datepicker box named Date bought
    And I should see a datepicker box named Date sold
    And I should see a 'CANCEL' button
    And I should see a 'View Stock' button

  Scenario: Able to see the view-historical-stock popup window in mobile view
    Given I am using the mobile view
    And I logged in to the portfolio page
    When I click the 'View Stock' button
    Then I should see a popup window titled 'View historical stock'
    And I should be able to click the 'Ticker' text input
    And I should be able to click the '# of shares' text input
    And I should be able to click the Date bought datepicker
    And I should be able to click the Date sold datepicker
    And I should be able to click the 'CANCEL' button in the popup
    And I should be able to click the 'View Stock' button in the popup
    
  Scenario: Cancel the add-historical-stock operation
    Given I logged in to the portfolio page
    When I click the 'View Stock' button
    And I click the 'CANCEL' button in the popup window
    Then The popup window should disappear

  Scenario: Fail to view stock due to malformed ticker
    Given I logged in to the portfolio page
    When I click the 'View Stock' button
    And I type 'AAPL1' to the 'Ticker' text input
    And I type '1' to the '# of shares' text input
    And I type '10/01/2020' to the 'Date bought' text input
    And I click the 'View Stock' button in the popup window
    Then I should be notified 'Ticker should consist of letters only and should not be more than 5 characters long' below the 'Ticker' text input

  Scenario: Fail to view stock due to missing ticker
    Given I logged in to the portfolio page
    When I click the 'View Stock' button
    And I type '1' to the '# of shares' text input
    And I type '10/01/2020' to the 'Date bought' text input
    And I click the 'View Stock' button in the popup window
    Then I should be notified 'Ticker field should not be empty' below the 'Ticker' text input

  Scenario: Fail to view stock due to nonexistent ticker
    Given I logged in to the portfolio page
    When I click the 'View Stock' button
    And I type 'AAAA' to the 'Ticker' text input
    And I type '1' to the '# of shares' text input
    And I type '10/01/2020' to the 'Date bought' text input
    And I click the 'View Stock' button in the popup window
    Then I should be notified 'Please enter a NASDAQ or NYSE stock ticker' below the 'Ticker' text input

  Scenario: Fail to view stock due to missing # of shares
    Given I logged in to the portfolio page
    When I click the 'View Stock' button
    And I type 'AAPL' to the 'Ticker' text input
    And I type '' to the '# of shares' text input
    And I type '10/01/2020' to the 'Date bought' text input
    And I click the 'View Stock' button in the popup window
    Then I should be notified 'Number of shares field should not be empty' below the '# of shares' text input

  Scenario: Fail to view stock due to negative # of shares
    Given I logged in to the portfolio page
    When I click the 'View Stock' button
    And I type 'AAPL' to the 'Ticker' text input
    And I type '-1' to the '# of shares' text input
    And I type '10/01/2020' to the 'Date bought' text input
    And I click the 'View Stock' button in the popup window
    Then I should be notified 'Number of shares should be a positive integer' below the '# of shares' text input

  Scenario: Fail to view stock due to malformed bought date
    Given I logged in to the portfolio page
    When I click the 'View Stock' button
    And I type 'AAPL' to the 'Ticker' text input
    And I type '1' to the '# of shares' text input
    And I type '2020-10-01' to the 'Date bought' text input
    And I click the 'View Stock' button in the popup window
    Then I should be notified 'Please enter a valid date of format MM/DD/YYYY' below the 'Date bought' text input

  Scenario: Fail to view stock due to missing bought date
    Given I logged in to the portfolio page
    When I click the 'View Stock' button
    And I type 'AAPL' to the 'Ticker' text input
    And I type '1' to the '# of shares' text input
    And I click the 'View Stock' button in the popup window
    Then I should be notified 'Date bought field should not be empty' below the 'Date bought' text input

  Scenario: Fail to view stock due to malformed sold date
    Given I logged in to the portfolio page
    When I click the 'View Stock' button
    And I type 'AAPL' to the 'Ticker' text input
    And I type '1' to the '# of shares' text input
    And I type '10/01/2020' to the 'Date bought' text input
    And I type '2020-10-10' to the 'Date sold' text input
    And I click the 'View Stock' button in the popup window
    Then I should be notified 'Please enter a valid date of format MM/DD/YYYY' below the 'Date sold' text input

  Scenario: Fail to view stock due to sold date earlier than bought date
    Given I logged in to the portfolio page
    When I click the 'View Stock' button
    And I type 'AAPL' to the 'Ticker' text input
    And I type '1' to the '# of shares' text input
    And I type '10/01/2020' to the 'Date bought' text input
    And I type '09/01/2020' to the 'Date sold' text input
    And I click the 'View Stock' button in the popup window
    Then I should be notified 'Date bought cannot be later than Date sold' below the 'Date bought' text input
    And I should be notified 'Date sold cannot be earlier than Date bought' below the 'Date sold' text input
    
  Scenario: Successfully view a portfolio stock with date sold specified
    Given I logged in to the portfolio page
    When I click the 'View Stock' button
    And I type 'AAPL' to the 'Ticker' text input
    And I type '1' to the '# of shares' text input
    And I type '09/01/2020' to the 'Date bought' text input
    And I type '10/01/2020' to the 'Date sold' text input
    And I click the 'View Stock' button in the popup window
    Then The popup window should disappear
    And The stock 'AAPL' should show up in my historical stocks
    And I should see AAPL appears in chart
    And I should see color of the 'AAPL' line matches that of its text entry
    And the time range of the chart starts at '2020-09-01'

  Scenario: Successfully view a portfolio stock with date sold unspecified
    Given I logged in to the portfolio page
    When I click the 'View Stock' button
    And I type 'TSLA' to the 'Ticker' text input
    And I type '1' to the '# of shares' text input
    And I type '09/01/2020' to the 'Date bought' text input
    And I click the 'View Stock' button in the popup window
    Then The popup window should disappear
    And The stock 'TSLA' should show up in my historical stocks
    And I should see TSLA appears in chart
    And I should see color of the 'TSLA' line matches that of its text entry
    And the time range of the chart starts at '2020-09-01'

  Scenario: Successfully view a portfolio stock with dates specified using calendar popups
    Given I logged in to the portfolio page
    When I click the 'View Stock' button
    And I type 'BILI' to the 'Ticker' text input
    And I type '1' to the '# of shares' text input
    And I click the calendar icon next to the 'Date bought' text input
    And I select day 2 of current month in the calendar popup
    And I click the 'OK' button in the calendar popup
    And I click the calendar icon next to the 'Date sold' text input
    And I select day 3 of current month in the calendar popup
    And I click the 'OK' button in the calendar popup
    And I click the 'View Stock' button in the popup window
    Then The popup window should disappear
    And The stock 'BILI' should show up in my historical stocks
    And I should see BILI appears in chart
    And I should see color of the 'BILI' line matches that of its text entry
    And the time range of the chart starts at '2020-11-02'
