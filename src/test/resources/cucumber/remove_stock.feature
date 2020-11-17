Feature: Remove a stock from my portfolio
  Scenario: Able to see the DELETE button next to the stock item
    Given I logged in to the portfolio page
    And I have some AAPL stocks in my portfolio stocks
    Then I should see the 'DELETE' button next to AAPL stocks in the portfolio stocks

  Scenario: Able to see the delete-stock confirmation popup window
    Given I logged in to the portfolio page
    And I have some AAPL stocks in my portfolio stocks
    When I click the 'DELETE' button next to AAPL stock in the portfolio stocks
    Then I should see a popup window titled 'Are you sure you want to remove AAPL?'
    And I should see text 'AAPL will be removed from your portfolio. This cannot be undone.' in the popup window
    And I should see a 'CANCEL' button
    And I should see a 'Delete Stock' button

  Scenario: Able to see the delete-stock confirmation popup window in mobile view
    Given I logged in to the portfolio page
    And I have some AAPL stocks in my portfolio stocks
    When I click the 'DELETE' button next to AAPL stock in the portfolio stocks
    Then I should see a popup window titled 'Are you sure you want to remove AAPL?'
    And I should see text 'AAPL will be removed from your portfolio. This cannot be undone.' in the popup window
    And I should be able to click the 'CANCEL' button in the popup
    And I should be able to click the 'Delete Stock' button in the popup

  Scenario: Cancel the remove operation in the confirmation popup window
    Given I logged in to the portfolio page
    And I have some AAPL stocks in my portfolio stocks
    When I click the 'DELETE' button next to AAPL stock in the portfolio stocks
    And I click the 'CANCEL' button in the popup window
   	Then The popup window should disappear

  Scenario: Successfully remove a stock from my portfolio
    Given I logged in to the portfolio page
    And I have some AAPL stocks in my portfolio stocks
    When I click the 'DELETE' button next to AAPL stock in the portfolio stocks
    And I click the 'Delete Stock' button in the popup window
    Then The popup window should disappear
    Then I should no longer see the AAPL stock in the portfolio stocks
    Then the time range of the chart starts at "3 months ago"

  Scenario: Successfully remove a stock from my historical
    Given I logged in to the portfolio page
    And I have some AAPL stocks in my historical stocks
    When I click the 'DELETE' button next to AAPL stock in the historical stocks
    And I click the 'Delete Stock' button in the popup window
    Then The popup window should disappear
    Then I should no longer see the AAPL stock in the historical stocks
    Then the time range of the chart starts at "3 months ago"
