Feature: Select / Unselect stocks in the portfolio page
  
  Since all scenarios in this feature requires complicated setup, we will run
  all the steps in the same browser window to save time.

  Scenario: After adding stocks, they should show up in the chart and the corresponding checkbox should be checked by default
    Given I logged in to the portfolio page
    And I have some AAPL stocks in my portfolio stocks
    And I have some TSLA stocks in my portfolio stocks
    And I have some AMZN stocks in my historical stocks
    And I have some TSLA stocks in my historical stocks
    Then I should see AMZN appears in chart
    And I should see TSLA appears in chart
    And The checkbox next to AAPL should be checked in my portfolio stocks
    And The checkbox next to TSLA should be checked in my portfolio stocks
    And The checkbox next to AMZN should be checked in my historical stocks
    And The checkbox next to TSLA should be checked in my historical stocks
  
  Scenario: Unchecking a historical stock removes it from the chart
    When I uncheck AMZN in my historical stocks
    Then I should see AMZN disappears from chart
  
  Scenario: Unselect all historical stocks
    When I click the 'Unselect all' button above my historical stocks
    Then I should see TSLA disappears from chart
    And The checkbox next to AMZN should be unchecked in my historical stocks
    And The checkbox next to TSLA should be unchecked in my historical stocks
  
  Scenario: Check a historical stock adds it to the chart
    When I check AMZN in my historical stocks
    Then I should see AMZN appears in chart
  
  Scenario: Select all historical stocks
    When I click the 'Select all' button above my historical stocks
    Then I should see TSLA appears in chart
    And The checkbox next to AMZN should be checked in my historical stocks
    And The checkbox next to TSLA should be checked in my historical stocks

  Scenario: Unchecking a portfolio stock updates the chart
    When I uncheck AAPL in my portfolio stocks
    Then The portfolio line in the chart should be updated
  
  Scenario: Unselect all portfolio stocks
    When I click the 'Unselect all' button above my portfolio stocks
    Then The portfolio line in the chart should be updated
    And The checkbox next to AAPL should be unchecked in my portfolio stocks
    And The checkbox next to TSLA should be unchecked in my portfolio stocks
  
  Scenario: Checking a portfolio stock updates the chart
    When I check AAPL in my portfolio stocks
    Then The portfolio line in the chart should be updated
  
  Scenario: Select all portfolio stocks
    When I click the 'Select all' button above my portfolio stocks
    Then The portfolio line in the chart should be updated
    And The checkbox next to AAPL should be checked in my portfolio stocks
    And The checkbox next to TSLA should be checked in my portfolio stocks