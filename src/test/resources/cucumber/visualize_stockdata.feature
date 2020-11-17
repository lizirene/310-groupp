Feature: Chart features

  Scenario: Default time range is 3 months when there is no stock
    Given I logged in to the portfolio page
    When the user has no portfolio stocks
    Then the time range of the chart starts at "3 months ago"

  Scenario: choose frequency by range selector
    Given I logged in to the portfolio page
    When I select "1m" range selector
    Then the time range of the chart starts at "1 month ago"

  Scenario: invalid start datepicker
    Given I logged in to the portfolio page
    When I input "1313231" in the start date datepicker of chart
    Then the time range of the chart starts at "original"

  Scenario: invalid end datepicker
    Given I logged in to the portfolio page
    When I input "1313231" in the end date datepicker of chart
    Then the time range of the chart ends at "original"

  Scenario: out of range start datepicker
    Given I logged in to the portfolio page
    When I input "2019-01-01" in the start date datepicker of chart
    Then the time range of the chart starts at "1 year ago"

  Scenario: out of range end datepicker
    Given I logged in to the portfolio page
    When I input "2021-01-01" in the end date datepicker of chart
    Then the time range of the chart ends at "today"

  Scenario: end date is before start date
    Given I logged in to the portfolio page
    When I input "2020-08-03" in the start date datepicker of chart
    And I input "2020-08-02" in the end date datepicker of chart
    Then the time range of the chart ends at "original"

  Scenario: valid start datepicker within range
    Given I logged in to the portfolio page
    When I input "2020-01-01" in the start date datepicker of chart
    Then the time range of the chart starts at "2020-01-01"

  Scenario: valid end date picker within range
    Given I logged in to the portfolio page
    When I input "2020-09-01" in the start date datepicker of chart
    And I input "2020-10-01" in the end date datepicker of chart
    Then the time range of the chart ends at "2020-10-01"

  Scenario: zoom in
    Given I logged in to the portfolio page
    When I click zoom in button in chart
    Then I should see the time range shrinks

  Scenario: zoom out
    Given I logged in to the portfolio page
    When I click zoom out button in chart
    Then I should see the time range expands