Feature: Upload csv to update portfolio stocks
  Scenario: Able to see the upload csv popup window
    Given I logged in to the portfolio page
    When I click the 'Upload Portfolio' button
    Then I should see a popup window titled 'Upload Portfolio From File'
    And I should see a file upload field
    And I should see a 'Cancel' button
    And I should see a 'Upload File' button

  Scenario: Able to see the upload csv popup window in mobile view
    Given I am using the mobile view
    And I logged in to the portfolio page
    When I click the 'Upload Portfolio' button
    Then I should see a popup window titled 'Upload Portfolio From File'
    And I should be able to click the file upload field
    And I should be able to click the 'Cancel' button
    And I should be able to click the 'Upload File' button

  Scenario: Fail to upload file with wrong content type
    Given I logged in to the portfolio page
    When I click the 'Upload Portfolio' button
    And I click the 'Cancel' button in the popup window
    Then The popup window should disappear

  Scenario: Fail to upload file with wrong content type
    Given I logged in to the portfolio page
    When I click the 'Upload Portfolio' button
    And I select 'wrong_content_type_1.json' in the file upload field
    And I click the 'Upload File' button in the popup window
    Then I should be notified 'Please upload a CSV file' below the file upload field

  Scenario: Fail to upload file with wrong header
    Given I logged in to the portfolio page
    When I click the 'Upload Portfolio' button
    And I select 'wrong_headers_1.csv' in the file upload field
    And I click the 'Upload File' button in the popup window
    Then I should be notified 'Please make sure the CSV\'s field names are correct: ticker, quantity, dateBought, dateSold' below the file upload field

  Scenario: Fail to upload file with missing data
    Given I logged in to the portfolio page
    When I click the 'Upload Portfolio' button
    And I select 'wrong_missing_data_1.csv' in the file upload field
    And I click the 'Upload File' button in the popup window
    Then I should be notified 'Some data in the CSV file are missing.' below the file upload field

  Scenario: Fail to upload file with invalid ticker
    Given I logged in to the portfolio page
    When I click the 'Upload Portfolio' button
    And I select 'wrong_invalid_ticker_1.csv' in the file upload field
    And I click the 'Upload File' button in the popup window
    Then I should be notified 'Found invalid ticker. Please check if all tickers are valid NASDAQ or NYSE stock tickers.' below the file upload field

  Scenario: Fail to upload file with invalid quantity
    Given I logged in to the portfolio page
    When I click the 'Upload Portfolio' button
    And I select 'wrong_invalid_quantity_1.csv' in the file upload field
    And I click the 'Upload File' button in the popup window
    Then I should be notified 'Found non-positive quantity or malformed quantity value. Please check that all quantity fields are positive integers.' below the file upload field

  Scenario: Fail to upload file with invalid date
    Given I logged in to the portfolio page
    When I click the 'Upload Portfolio' button
    And I select 'wrong_invalid_date_1.csv' in the file upload field
    And I click the 'Upload File' button in the popup window
    Then I should be notified 'Cannot parse some of the date values. Please check that all dateBought and dateSold fields are valid date of format MM/DD/YYYY.' below the file upload field

  Scenario: Fail to upload file with date bought later than date sold
    Given I logged in to the portfolio page
    When I click the 'Upload Portfolio' button
    And I select 'wrong_date_bought_later_than_date_sold_1.csv' in the file upload field
    And I click the 'Upload File' button in the popup window
    Then I should be notified 'Find dateBought later than dateSold. Please check for each entry, dateBought is no later than dateSold.' below the file upload field

  Scenario: Successfully uploading csv file updates portfolio stocks
    Given I logged in to the portfolio page
    When I click the 'Upload Portfolio' button
    And I select 'correct_1.csv' in the file upload field
    And I click the 'Upload File' button in the popup window
    Then All stocks in 'correct_1.csv' should appear in the portfolio stocks