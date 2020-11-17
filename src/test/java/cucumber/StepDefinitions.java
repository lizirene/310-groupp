package cucumber;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import csci310.trading.StockEndpoint;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import csci310.Database.DatabaseAPI;
import csci310.trading.StockTrading;

import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

class VisibilityOfAnyElement implements ExpectedCondition<WebElement> {
    private By by;

    public VisibilityOfAnyElement(By by) {
        this.by = by;
    }

    @Override
    public WebElement apply(WebDriver driver) {
        List<WebElement> elements = driver.findElements(by);
        for (WebElement element : elements) {
            if (element.isDisplayed() && element.isEnabled()) {
                return element;
            }
        }
        return null;
    }
}

class DataErrorBecomes implements ExpectedCondition<WebElement> {
    private String text;
    private WebElement label;
    private boolean islabel;

    public DataErrorBecomes(WebElement label, String text) {
        this.label = label;
        this.text = text;
        this.islabel = true;
    }

    public DataErrorBecomes(WebElement label, String text, boolean islabel) {
        this.label = label;
        this.text = text;
        this.islabel = islabel;
    }

    @Override
    public WebElement apply(WebDriver driver) {
        WebElement notification = islabel ? label.findElement(By.xpath("following-sibling::span[@class=\"helper-text\"]")) : label;
        if (notification.getAttribute("data-error").trim().equalsIgnoreCase(text)) {
            return notification;
        }
        return null;
    }
}

class Stock extends csci310.model.Stock {
    public boolean checked;
    public WebElement element;
    public WebElement deleteButton;
    public WebElement checkbox;

    public final static DateTimeFormatter dateFieldFormatter = DateTimeFormatter.ofPattern("M/d/uuuu");

    public Stock(WebDriver driver, WebElement element) {
        super(0, "N/A", 0, "lunjohn", new Date());
        this.element = element;
        String ticker = element.findElement(By.className("chip")).getText();
        setTicker(ticker.trim().toUpperCase());
        checkbox = element.findElement(By.cssSelector(".collection-item input[type=\"checkbox\"]"));
        checked = checkbox.isSelected();
        checkbox = checkbox.findElement(By.xpath("following-sibling::span"));
        deleteButton = element.findElement(By.cssSelector(".btn, .btn-small"));

        // Optionally get date bought and date sold
        List<WebElement> dates = element.findElements(By.className("stock-date"));
        if (!dates.isEmpty()) {
            dates = dates.get(0).findElements(By.tagName("span"));
            setDateBought(Date.from(LocalDate.parse(dates.get(0).getText().trim(), Stock.dateFieldFormatter).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            if (dates.size() > 1) {
                String sold = dates.get(1).getText().replace("→", "").trim();
                setDateSold(Date.from(LocalDate.parse(sold, Stock.dateFieldFormatter).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
        }
    }
}

class Range {
    public LocalDate startDate = LocalDate.now().minusMonths(3);
    public LocalDate endDate = LocalDate.now();
}

/**
 * Step definitions for Cucumber tests.
 */
public class StepDefinitions {
    private static final String ROOT_URL = "https://localhost:8080/";
    private static final Pattern rgbaRegex = Pattern.compile("rgba?\\((\\d+),(\\d+),(\\d+)(,\\d+)?\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern dateRegex = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static boolean hasInit = false;
    private static boolean reuseDriver = false;
    private static int portfolioUserCount = 0;
    private static List<Double> previousStockValues;

    private static WebDriver driver;
    private static JavascriptExecutor jse;
    private static WebDriverWait wait10s;

    private Range range = new Range();

    //#region Constructor and helper functions
    public StepDefinitions() throws ClassNotFoundException, SQLException {
        if (hasInit)
            return;
        hasInit = true;
        System.out.println("Clear database");
        DatabaseAPI.getInstance().clearDB();
    }

    private void waitForLoading() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        wait10s.until(ExpectedConditions.invisibilityOfElementLocated(By.className("fullscreen-loader")));
    }

    private String getRootText(WebElement element) {
        String text = element.getText();
        for (WebElement child : element.findElements(By.xpath("./*"))) {
            text = text.replaceFirst(child.getText(), "");
        }
        return text;
    }

    private WebElement findButtonByText(String buttonText, String parentCssSelector) {
        waitForLoading();
        List<WebElement> elements;
        if (parentCssSelector == null) {
            wait10s.until(ExpectedConditions.visibilityOfElementLocated(By.className("btn")));
            elements = driver.findElements(By.cssSelector(".btn, .btn-flat, .datepicker-day-button"));
        } else {
            WebElement parent = wait10s.until(new VisibilityOfAnyElement(By.cssSelector(parentCssSelector)));
            elements = parent.findElements(By.cssSelector(".btn, .btn-flat, .datepicker-day-button"));
        }
        for (WebElement element : elements) {
            if (getRootText(element).trim().equalsIgnoreCase(buttonText)) {
                return element;
            }
        }
        return null;
    }

    private WebElement findLabelByText(String labelText) {
        waitForLoading();
        List<WebElement> elements = driver.findElements(By.tagName("label"));
        for (WebElement element : elements) {
            if (element.getText().trim().equalsIgnoreCase(labelText)) {
                return element;
            }
        }
        return null;
    }

    private List<String> findAllStockTickersinChart() {
        return (List<String>) jse.executeScript("return Highcharts.charts[0].series.filter(e => !e.name.startsWith(\"Navigator\")).map(e => e.name)\n");
    }

    private String findHistoricalStockColorinChart(String ticker) {
        String executable = String.format("return Highcharts.charts[0].series.filter(e => e.name == \"%s\")[0].color", ticker);
        return (String) jse.executeScript(executable);
    }

    private String findHistoricalStockColorinList(String ticker) {
        return wait10s.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.cssSelector("#historical-stock-list > div > div.flex-column > label > span > span"))).stream()
                .filter(e -> e.getText().equalsIgnoreCase(ticker))
                .findFirst()
                .map(e -> e.getAttribute("style").split(":")[1].replaceAll(";", "").trim())
                .orElse(null);
    }

    public String getLastTradingDate() {
        try {
            StockTrading st = new StockTrading();

            // Use Google stock 'goog' as example
            String result = st.getStockMetadata("goog");
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> jsonResult = new Gson().fromJson(result, type);
            LocalDate date = LocalDate.parse(jsonResult.get("endDate"));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String dateString = date.format(formatter);
            return dateString;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getSecondToLastTradingDate() {
        try {
            StockTrading st = new StockTrading();

            // Use Google stock 'goog' as example
            List<StockEndpoint> example = st.getHistoricalPrices(
                    "goog",
                    LocalDate.now().minus(5, ChronoUnit.DAYS).toString(),
                    LocalDate.now().toString(),
                    StockTrading.ResampleFreq.DAILY);
            LocalDate date = LocalDate.parse(example.get(example.size() - 2).date.substring(0, 10));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String dateString = date.format(formatter);
            return dateString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int[] getRGB(WebElement element) {
        String color = element.getCssValue("color");
        String[] rgbValue = color.replace("rgba(", "").replace(")", "").split(",");
        int r = Integer.parseInt(rgbValue[0].trim());
        int g = Integer.parseInt(rgbValue[1].trim());
        int b = Integer.parseInt(rgbValue[2].trim());
        int[] rgb = { r, g, b };
        return rgb;
    }

    private WebElement getFileInput() {
        WebElement popup = wait10s.until(new VisibilityOfAnyElement(By.className("modal")));
        return popup.findElement(By.cssSelector("input[type=\"file\"]"));
    }

    private List<Stock> getStocks(boolean portfolio) {
        waitForLoading();
        WebElement element = driver.findElement(By.id((portfolio ? "portfolio" : "historical") + "-stock-list"));
        List<WebElement> items = element.findElements(By.cssSelector(".collection-item.stock-item"));
        return items.stream().map(item -> new Stock(driver, item)).collect(Collectors.toList());
    }

    private List<Stock> getStocks(boolean portfolio, String ticker) {
        return getStocks(portfolio).stream()
            .filter(stock -> stock.getTicker().equalsIgnoreCase(ticker))
            .collect(Collectors.toList());
    }

    private Stock getStock(boolean portfolio, String ticker) {
        List<Stock> stocks = getStocks(portfolio, ticker);
        return stocks.isEmpty() ? null : stocks.get(0);
    }

    private List<Double> getStockValuesOnChart() {
        List<String> values = (List<String>) jse.executeScript("return Highcharts.charts[0].series.find(e => e.name === 'Portfolio').data.filter(e => e).map(e => e.options.y.toString())");

        return values.stream().map(c -> Double.parseDouble(c)).collect(Collectors.toList());
    }
    
    private String getFeatureFileNameFromScenarioId(Scenario scenario) {
        String[] tab = scenario.getId().split("/");
        int rawFeatureNameLength = tab.length;
        String featureName = tab[rawFeatureNameLength - 1].split(":")[0];
        System.out.println("featureName: " + featureName);
        return featureName;
    }

    private String processDate(String date) {
        // "Aug  7, 2020" -> "Aug 07, 2020"
        // "Aug 17, 2020" -> "Aug 17, 2020"
        StringBuilder builder = new StringBuilder(date);
        if (date.length() == 11) {
            builder.insert(4, '0');
        }
        return builder.toString();
    }

    private void updateRange() {
        List<WebElement> datepickers = wait10s.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".highcharts-label.highcharts-range-input")));
        // update range
        range.startDate = LocalDate.parse(processDate(datepickers.get(0).getText()), dateFormat);
        range.endDate = LocalDate.parse(processDate(datepickers.get(1).getText()), dateFormat);
    }
    //#endregion

    //#region Scenario setup
    @Before
    public void beforeScenario(Scenario scenario) throws InterruptedException {
        if (reuseDriver && !getFeatureFileNameFromScenarioId(scenario).startsWith("select_stock")) {
            // Special case, reuse webdriver to reduce testing time
            Thread.sleep(1000);
            driver.quit();
            reuseDriver = false;
        }

        if (!reuseDriver) {
            ChromeOptions options = new ChromeOptions();
            // Ignore ssl error
            options.addArguments("ignore-certificate-errors");
    
            // Mobile view
            if (scenario.getName().contains("mobile view")) {
                Map<String, String> mobileEmulation = new HashMap<>();
                mobileEmulation.put("deviceName", "Pixel 2");
                options.setExperimentalOption("mobileEmulation", mobileEmulation);
                options.addArguments("--window-size=360,960");
            }
    
            // Setup driver
            driver = new ChromeDriver(options);
            jse = (JavascriptExecutor) driver;
            wait10s = new WebDriverWait(driver, 10);
        }

        if (getFeatureFileNameFromScenarioId(scenario).startsWith("select_stock")) {
            // Special case, reuse webdriver to reduce testing time
            reuseDriver = true;
        }
    }
    //#endregion

    //#region Given: register and login
    @Given("I am on the {word} page")
    public void i_am_on_the_page(String word) {
        driver.get(ROOT_URL + word + ".jsp");
    }

    @Given("The account {string} with password {string} already exists")
    public void the_account_with_password_already_exists(String username, String password)
            throws SQLException, ClassNotFoundException, NoSuchAlgorithmException {
        DatabaseAPI api = DatabaseAPI.getInstance();
        if (api.getUser(username) == null)
            api.addUser(username, password);
    }

    @Given("the account with username {string} doesn't exist yet")
    public void the_account_with_username_doesn_t_exist_yet(String username)
            throws SQLException, ClassNotFoundException, NoSuchAlgorithmException {
        DatabaseAPI api = DatabaseAPI.getInstance();
        if (api.getUser(username) != null) {
            api.deleteUser(username);
        }
    }

    @Given("I am using the mobile view")
    public void i_am_using_the_mobile_view() {
        // Nothing to do, handled in before scenario hook.
    }
    //#endregion

    //#region Given: portfolio
    @Given("I logged in to the portfolio page")
    public void i_logged_in_to_the_portfolio_page() throws ClassNotFoundException, NoSuchAlgorithmException, SQLException {
        String testUsername = "testportfolio" + (++portfolioUserCount);
        String testPassword = "dddddddd";
        the_account_with_password_already_exists(testUsername, testPassword);
        i_am_on_the_page("login");
        i_type_to_the_text_input(testUsername, "Username");
        i_type_to_the_text_input(testPassword, "Password");
        i_click_the_button("login");
        i_should_be_redirected_to_the_page("portfolio");
    }
    
    @Given("I have some {word} stocks in my {word} stocks")
    public void i_have_some_stocks_in_my_stocks(String stock, String type) {
        String buttonText = type.equalsIgnoreCase("portfolio") ? "Add Stock" : "View Stock";
    	i_click_the_button(buttonText);
    	i_type_to_the_text_input(stock, "ticker");
    	i_type_to_the_text_input("3", "# of shares");
    	i_type_to_the_text_input("09/01/2020", "Date bought");
    	i_type_to_the_text_input("10/01/2020", "Date sold");
    	i_click_the_button_in_the_popup_window(buttonText);
    	the_stock_should_show_up_in_my_stocks(stock, type);
    }
    //#endregion

    //#region Given: security
    @Given("I enter the portfolio page without logging in")
    public void i_enter_the_portfolio_page_without_logging_in() {
        driver.get(ROOT_URL + "portfolio.jsp");
    }

    @Given("I access the index page using http")
    public void i_access_the_home_page_using_http() {
        driver.get(ROOT_URL.replaceFirst("https://", "http://"));
    }

    @Given("I access the index page")
    public void i_access_the_home_page() throws InterruptedException {
        driver.navigate().to(ROOT_URL);
        Thread.sleep(1000);
    }
    //#endregion

    //#region When: login and register
    @When("I click the {string} button")
    public void i_click_the_button(String buttonText) {
        WebElement button = findButtonByText(buttonText, null);
        wait10s.until(ExpectedConditions.elementToBeClickable(button)).click();
    }

    @When("I type {string} to the {string} text input")
    public void i_type_to_the_text_input(String string, String labelText) {
        WebElement label = findLabelByText(labelText);
        WebElement input = label.findElement(By.xpath("preceding-sibling::input[not(@type=\"hidden\")]"));
        input.sendKeys(Keys.HOME, Keys.chord(Keys.SHIFT, Keys.END), Keys.BACK_SPACE, string);
    }
    //#endregion

    //#region When: portfolio
    @When("I type the last trading day to the {string} text input")
    public void i_type_the_last_trading_day_to_the_text_input(String labelText) {
        String lastTradingDay = getLastTradingDate();
        i_type_to_the_text_input(lastTradingDay, labelText);
    }

    @When("I type the second to last trading day to the {string} text input")
    public void i_type_the_second_to_last_trading_day_to_the_text_input(String labelText) {
        String secondToLastTradingDay = getSecondToLastTradingDate();
        i_type_to_the_text_input(secondToLastTradingDay, labelText);
    }

    @When("I click the {string} button in the popup window")
    public void i_click_the_button_in_the_popup_window(String buttonText) {
        findButtonByText(buttonText, ".modal").click();
    }

    @When("I click the calendar icon next to the {string} text input")
    public void i_click_the_calendar_icon_next_to_the_text_input(String labelText) throws InterruptedException {
        WebElement label = findLabelByText(labelText);
        WebElement icon = label.findElement(By.xpath("preceding-sibling::a[contains(@class, \"prefix\")]"));
        icon.click();
    }

    @When("I click the {string} button in the calendar popup")
    public void i_click_the_button_in_the_calendar_popup(String buttonText) {
        findButtonByText(buttonText, ".datepicker-container").click();
        wait10s.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.className("datepicker-modal"))));
    }

    @When("I select day {int} of current month in the calendar popup")
    public void i_select_day_of_current_month_in_the_calendar_popup(Integer day) {
        findButtonByText(day.toString(), ".datepicker-table").click();
    }

    @When("I select {string} in the file upload field")
    public void i_select_in_the_file_upload_field(String filename) {
        WebElement file = getFileInput();
        String path = System.getProperty("user.dir") + "/src/test/resources/test_csv/" + filename;
        file.sendKeys(path);
    }
    
    @When("I click the {string} button next to {word} stock in the {word} stocks")
    public void i_click_the_button_next_to_stock_in_the_portfolio_stocks(String button, String stock, String type) {
        getStock(type.equalsIgnoreCase("portfolio"), stock).deleteButton.click();
    }

    @When("I {word} {word} in my {word} stocks")
    public void i_uncheck_in_my_historical_stocks(String action, String stock, String type) {
        // get all stock values before check/uncheck
        previousStockValues = getStockValuesOnChart();

        Stock s = getStock(type.equalsIgnoreCase("portfolio"), stock);
        assertEquals(action.equalsIgnoreCase("uncheck"), s.checked);
        s.checkbox.click();
    }
    
    @When("I click the {string} button above my {word} stocks")
    public void i_click_the_button_above_my_portfolio_stocks(String buttonText, String type) {
        // get all stock values before clicking
        previousStockValues = getStockValuesOnChart();

        WebElement element = driver.findElements(By.cssSelector(":not(.flex-between).list-header")).get(type.equalsIgnoreCase("portfolio") ? 0 : 1);
        WebElement button = element.findElements(By.tagName("button")).stream().filter(e -> getRootText(e).equalsIgnoreCase(buttonText)).findFirst().get();
        wait10s.until(ExpectedConditions.elementToBeClickable(button));
        button.click();
    }
    //#endregion

    //#region When: chart
    @When("I select {string} range selector")
    public void i_select_range_selector(String frequency) {
        List<WebElement> rangeselectors = wait10s.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".highcharts-button")));
        rangeselectors.stream().filter(e -> e.getText().equalsIgnoreCase(frequency)).findFirst().ifPresent(element -> wait10s.until(ExpectedConditions.elementToBeClickable(element)).click());
    }

    @When("I click zoom {word} button in chart")
    public void i_click_button_in_chart(String buttonText) {
        List<WebElement> zooms = wait10s.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("#stock-chart-zoom > button")));
        if (buttonText.equalsIgnoreCase("in")) {
            wait10s.until(ExpectedConditions.elementToBeClickable(zooms.get(1))).click();
        } else if (buttonText.equalsIgnoreCase("out")) {
            wait10s.until(ExpectedConditions.elementToBeClickable(zooms.get(0))).click();
        }
    }

    @When("I input {string} in the {word} date datepicker of chart")
    public void i_input_in_the_date_datepicker_of_chart(String date, String start) {
        List<WebElement> datepickers = wait10s.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".highcharts-label.highcharts-range-input>text")));
        List<WebElement> inputdatepickers = wait10s.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".highcharts-range-selector")));
        WebElement datepicker = start.equalsIgnoreCase("start") ? datepickers.get(0) : datepickers.get(1);
        WebElement inputdatepicker = start.equalsIgnoreCase("start") ? inputdatepickers.get(0) : inputdatepickers.get(1);
        wait10s.until(ExpectedConditions.elementToBeClickable(datepicker)).click();

        try {
            wait10s.until(ExpectedConditions.elementToBeClickable(inputdatepicker));
            inputdatepicker.sendKeys(Keys.HOME, Keys.chord(Keys.SHIFT, Keys.END), Keys.BACK_SPACE, date, Keys.RETURN);
        } catch (org.openqa.selenium.ElementNotInteractableException e) {
            Long min = (Long) jse.executeScript("return Highcharts.charts[0].xAxis[0].min");
            Long max = (Long) jse.executeScript("return Highcharts.charts[0].xAxis[0].max");
            if (start.equalsIgnoreCase("start")) {
                jse.executeScript(String.format("Highcharts.charts[0].xAxis[0].setExtremes(%d, %d)", LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), max));
            } else {
                jse.executeScript(String.format("Highcharts.charts[0].xAxis[0].setExtremes(%d, %d)", min, LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            }
        }

        Actions action = new Actions(driver);
        action.moveToElement(driver.findElement(By.xpath("//*[@id=\"app\"]/div[1]/div[1]/h5"))).click().perform();
    }

    @When("the user has no portfolio stocks")
    public void the_user_has_no_portfolio_stocks() {
//        List<Double> values = (List<Double>) jse.executeScript("return Highcharts.charts[0].series.find(e => e.name === 'Portfolio').data");
    }
    //#endregion

    //#region When: security
    @When("I wait for {int} second")
    @When("I wait for {int} seconds")
    public void i_wait_for_seconds(Integer seconds) throws InterruptedException {
        Thread.sleep(seconds * 1000);
    }
    //#endregion

    //#region Then: login and register
    @Then("I should be redirected to the {word} page")
    public void i_should_be_redirected_to_the_page(String page) {
        wait10s.until(ExpectedConditions.urlContains("/" + page + ".jsp"));
        assertTrue(driver.getCurrentUrl().endsWith("/" + page + ".jsp"));
    }

    @Then("I should be able to click the {string} button")
    public void i_should_be_able_to_click_the_button(String buttonText) {
        WebElement button = findButtonByText(buttonText, null);
        wait10s.until(ExpectedConditions.elementToBeClickable(button));
    }

    @Then("I should be able to click the {string} button in the popup")
    public void i_should_be_able_to_click_the_button_in_the_popup(String buttonText) {
        WebElement button = findButtonByText(buttonText, ".modal");
        wait10s.until(ExpectedConditions.elementToBeClickable(button));
    }

    @Then("I should be able to click the {string} text input")
    public void i_should_be_able_to_click_the_text_input(String labelText) {
        WebElement label = findLabelByText(labelText);
        WebElement textField = label.findElement(By.xpath("preceding-sibling::input"));
        wait10s.until(ExpectedConditions.elementToBeClickable(textField));
    }
    //#endregion

    //#region Then: portfolio
    @Then("I should see a {string} button on the {word} page")
    public void i_should_see_a_button_on_the_page(String buttonText, String page) {
        wait10s.until(ExpectedConditions.urlContains("/" + page + ".jsp"));
        assertNotNull(findButtonByText(buttonText, null));
    }

    @Then("I should see a {string} button")
    public void i_should_see_a_button(String buttonText) {
        assertNotNull(findButtonByText(buttonText, ".modal"));
    }

    @Then("I should see a popup window titled {string}")
    public void i_should_see_a_popup_window_titled(String titleText) {
        WebElement element = wait10s.until(new VisibilityOfAnyElement(By.className("modal")));
        WebElement title = element.findElement(By.tagName("h4"));
        assertTrue(title.getText().equalsIgnoreCase(titleText));
    }

    @Then("I should see a text input named {string}")
    public void i_should_see_a_text_input_named(String labelText) {
        WebElement label = findLabelByText(labelText);
        assertNotNull(label.findElement(By.xpath("preceding-sibling::input")));
    }

    @Then("I should see a datepicker box named Date {word}")
    public void i_should_see_a_datepicker_box_named(String labelText) {
        WebElement label = findLabelByText("Date " + labelText);
        assertNotNull(label.findElement(By.xpath("preceding-sibling::input[@class=\"datepicker\"]")));
    }

    @Then("The popup window should disappear")
    public void the_popup_window_should_disappear() {
        List<WebElement> elements = driver.findElements(By.className("modal"));
        wait10s.until(ExpectedConditions.invisibilityOfAllElements(elements));
        assertTrue(driver.findElements(By.className("modal")).stream().allMatch(element -> !element.isDisplayed()));
    }

    @Then("I should be notified {string} below the {string} text input")
    public void i_should_be_notified_below_the_textbox(String string, String labelText) {
        WebElement label = findLabelByText(labelText);
        WebElement notification = wait10s.until(new DataErrorBecomes(label, string));
        assertNotNull(notification);
    }

    @Then("The stock {string} should show up in my {word} stocks")
    public void the_stock_should_show_up_in_my_stocks(String stock, String type) {
        assertTrue(getStocks(type.equalsIgnoreCase("portfolio")).stream().anyMatch(element -> element.getTicker().equalsIgnoreCase(stock)));
    }

    @Then("I should see a chart on the portfolio page")
    public void i_should_see_a_chart_on_the_portfolio_page() {
        WebElement chart = wait10s.until(ExpectedConditions.visibilityOfElementLocated(By.id("stock-chart")));
        assertNotNull(chart);
    }

    @Then("I should see a {string} button in the app bar")
    public void i_should_see_a_button_in_the_app_bar(String buttonText) {
        assertNotNull(findButtonByText(buttonText, "nav.appbar"));
    }

    @Then("I should be able to click the Date {word} datepicker")
    public void i_should_be_able_to_click_the_datepicker(String labelText) {
        WebElement label = findLabelByText("Date " + labelText);
        WebElement icon = label.findElement(By.xpath("preceding-sibling::a[contains(@class, \"prefix\")]"));
        wait10s.until(ExpectedConditions.elementToBeClickable(icon));
    }

    @Then("I should see a file upload field")
    public void i_should_see_a_file_upload_field() {
        assertNotNull(getFileInput());
    }

    @Then("I should be able to click the file upload field")
    public void i_should_be_able_to_click_the_file_upload_field() {
        WebElement button = getFileInput().findElement(By.xpath("./.."));
        wait10s.until(ExpectedConditions.elementToBeClickable(button));
    }

    @Then("I should be notified {string} below the file upload field")
    public void i_should_be_notified_below_the_file_upload_field(String string) {
        WebElement span = wait10s.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".file-path-wrapper .helper-text")));
        wait10s.until(new DataErrorBecomes(span, string, false));
    }

    @Then("All stocks in {string} should appear in the portfolio stocks")
    public void all_stocks_in_should_appear_in_the_portfolio_stocks(String filename) throws IOException {
        List<String> content = Files.readAllLines(Paths.get(System.getProperty("user.dir"), "src/test/resources/test_csv", filename));
        List<Stock> stocks = getStocks(true);
        for (int i = 1; i < content.size(); i++) {
            String ticker = content.get(i).split(",")[0];
            assertTrue(stocks.stream().anyMatch(stock -> stock.getTicker().equalsIgnoreCase(ticker)));
        }
    }
    
    @Then("I should see the {string} button next to {word} stocks in the {word} stocks")
    public void i_should_see_the_button_next_to_stocks_in_the_portfolio_stocks(String button, String stock, String type) {
        assertTrue(getRootText(getStock(type.equalsIgnoreCase("portfolio"), stock).deleteButton).equalsIgnoreCase(button));
    }
    
    @Then("I should no longer see the {word} stock in the {word} stocks")
    public void i_should_no_longer_see_the_stock_in_the_portfolio_stocks(String stock, String type) {
        assertNull(getStock(type.equalsIgnoreCase("portfolio"), stock));
    }
    
    @Then("I should see text {string} in the popup window")
    public void i_should_see_text_in_the_popup_window(String text) {
    	 WebElement element = wait10s.until(new VisibilityOfAnyElement(By.className("modal")));
         WebElement title = element.findElement(By.tagName("p"));
         assertTrue(title.getText().equalsIgnoreCase(text));
    }


    @Then("My stock portfolio value should {word} with color being {word}")
    public void my_stock_portfolio_value_should_with_color_being(String numStatus, String color) {
        WebElement element = wait10s.until(ExpectedConditions.visibilityOfElementLocated(By.className("title-value")));
        int[] rgb = getRGB(element);
        Double value = Double.parseDouble(element.getText().split("\\$")[1].trim());

        if(numStatus.equals("increase") && color.equals("green")) {
            assertTrue(value > 0);
            assertEquals(0, rgb[0]);
            assertEquals(128, rgb[1]);
            assertEquals(0, rgb[2]);
        }
        else if(numStatus.equals("decrease") && color.equals("red")) {
            assertTrue(value == 0);
            assertEquals(255, rgb[0]);
            assertEquals(0, rgb[1]);
            assertEquals(0, rgb[2]);
        }
    }

    @Then("The percentage value should be positive and be {word}")
    public void the_percentage_value_should_be_positive_and_be(String color) {
        WebElement element = wait10s.until(ExpectedConditions.visibilityOfElementLocated(By.className("title-value-percentage")));
        int[] rgb = getRGB(element);
        Double percentage = Double.parseDouble(element.getText().split(" ")[1].split("%")[0]);
        assertTrue(percentage > 0);

        if(color.equals("green")) {
            assertEquals(0, rgb[0]);
            assertEquals(128, rgb[1]);
            assertEquals(0, rgb[2]);
        }
        else if (color.equals("red")) {
            assertEquals(255, rgb[0]);
            assertEquals(0, rgb[1]);
            assertEquals(0, rgb[2]);
        }
    }

    @Then("The arrow should be {word} and points {word}")
    public void the_arrow_should_be_and_points(String color, String direction) {
        WebElement element = wait10s.until(ExpectedConditions.visibilityOfElementLocated(By.className("title-value-percentage")));
        int[] rgb = getRGB(element);
        String arrow = element.getText().split(" ")[0];
        System.out.println(arrow);
        if(color.equals("green") && direction.equals("upward")) {
            assertEquals("▲", arrow);
            assertEquals(0, rgb[0]);
            assertEquals(128, rgb[1]);
            assertEquals(0, rgb[2]);
        }
        else if(color.equals("red") && direction.equals("downward")) {
            assertEquals("▼", arrow);
            assertEquals(255, rgb[0]);
            assertEquals(0, rgb[1]);
            assertEquals(0, rgb[2]);
        }
    }

    @Then("The stock {string} should show a bought date of {string}")
    public void the_stock_should_show_bought_date(String stock, String dateBought) {
        Date expectedDateBought = Date.from(LocalDate.parse(dateBought, Stock.dateFieldFormatter).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Stock s = getStock(true, stock);
        assertEquals(expectedDateBought, s.getDateBought());
    }

    @Then("The stock {string} should show a bought date of {string} and a sold date of {string}")
    public void the_stock_should_show_bought_date_and_sold_date(String stock, String dateBought, String dateSold) {
        the_stock_should_show_bought_date(stock, dateBought);
        Date expectedDateSold = Date.from(LocalDate.parse(dateSold, Stock.dateFieldFormatter).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Stock s = getStock(true, stock);
        assertEquals(expectedDateSold, s.getDateSold());
    }
    
    @Then("The stock {string} should show a bought date of day {int} of current month and a sold date of day {int} of current month")
    public void the_stock_should_show_a_bought_date_of_day_of_current_month_and_a_sold_date_of_day_of_current_month(String stock, Integer dayBought, Integer daySold) {
        LocalDate bought = LocalDate.now();
        LocalDate sold = LocalDate.now();
        bought = bought.with(TemporalAdjusters.firstDayOfMonth());
        sold = sold.with(TemporalAdjusters.firstDayOfMonth());
        bought = bought.plusDays(dayBought - 1);
        sold = sold.plusDays(daySold - 1);
        String boughtString = bought.format(Stock.dateFieldFormatter);
        String soldString = sold.format(Stock.dateFieldFormatter);
        the_stock_should_show_bought_date_and_sold_date(stock, boughtString, soldString);
    }
    //#endregion

    //#region Then: chart
    @Then("I should see {string} date to be {string} than {string}")
    public void i_should_see_date_to_be_than_provideddate(String date, String earlier, String provided) throws ParseException {
        List<WebElement> datepickers = wait10s.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".highcharts-label.highcharts-range-input")));
        SimpleDateFormat startDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat providedDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date startDate = startDateFormat.parse(date.equalsIgnoreCase("start") ?
                datepickers.get(0).getText() :
                datepickers.get(1).getText());
        Date providedDate = providedDateFormat.parse(provided);
        if (earlier.equalsIgnoreCase("earlier")) {
            assertTrue(startDate.compareTo(providedDate) < 0);
        } else if (earlier.equalsIgnoreCase("later")) {
            assertTrue(startDate.compareTo(providedDate) > 0);
        }
    }

    @Then("the time range of the chart {word} at {string}")
    public void i_should_see_datepicker_becomes(String start, String time) {
        List<WebElement> datepickers = wait10s.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".highcharts-label.highcharts-range-input")));
        WebElement datepicker = start.equalsIgnoreCase("starts") ? datepickers.get(0) : datepickers.get(1);
        String date = processDate(datepicker.getText());

        Matcher m = dateRegex.matcher(time);
        LocalDate targetDate = LocalDate.parse(date, dateFormat);
        if (m.matches()) {
            // specific date
            assertEquals(LocalDate.parse(time), targetDate);
        } else {
            // frequency
            if (time.equalsIgnoreCase("original")) {
                assertTrue(Period.between(start.equalsIgnoreCase("start") ?
                        range.startDate :
                        range.endDate, targetDate).getDays() <= 1);
            } else if (time.equalsIgnoreCase("3 months ago")) {
                assertTrue(Period.between(LocalDate.now().minusMonths(3), targetDate).getDays() <= 1);
            } else if (time.equalsIgnoreCase("1 month ago")) {
                assertTrue(Period.between(LocalDate.now().minusMonths(1), targetDate).getDays() <= 1);
            } else if (time.equalsIgnoreCase("1 year ago")) {
                assertTrue(Period.between(LocalDate.now().minusYears(1), targetDate).getDays() <= 3);
            }
        }

        updateRange();
    }

    @Then("I should see {word} appears in chart")
    public void i_should_see_exists_in_chart(String stock) {
        assertTrue(findAllStockTickersinChart().stream().anyMatch(e -> e.equalsIgnoreCase(stock)));
    }

    @Then("I should see {word} disappears from chart")
    public void i_should_see_does_not_exist_in_chart(String stock) {
        assertFalse(findAllStockTickersinChart().stream().anyMatch(e -> e.equalsIgnoreCase(stock)));
    }

    @Then("I should see color of the {string} line matches that of its text entry")
    public void i_should_see_color_of_the_line_matches_that_of_its_text_entry(String stock) {
        String color1 = findHistoricalStockColorinChart(stock);
        String color2 = findHistoricalStockColorinList(stock);
        Color color1Object = Color.decode(color1);
        Matcher m = rgbaRegex.matcher(color2.replaceAll("\\s+", ""));
        if (m.matches()) {
            Color color2Object = new Color(Integer.parseInt(m.group(1)),  // r
                    Integer.parseInt(m.group(2)),  // g
                    Integer.parseInt(m.group(3))); // b
            assertEquals(color1Object, color2Object);
        } else {
            fail();
        }
    }

    @Then("The checkbox next to {word} should be {word} in my {word} stocks")
    public void should_be_checked_in_my_historical_stocks(String stock, String state, String type) {
        assertTrue(getStock(type.equalsIgnoreCase("portfolio"), stock).checked == (state.equalsIgnoreCase("checked")));
    }

    @Then("The portfolio line in the chart should be updated")
    public void the_portfolio_line_in_the_chart_should_be_updated() {
        List<Double> currStockValues = getStockValuesOnChart();

        if((previousStockValues == null) != (currStockValues == null))
            return;

        // size has changed
        if(currStockValues.size() != previousStockValues.size()) {
            return;
        }

        // any member changed
        for(int i=0; i<currStockValues.size(); i++) {
//            System.out.println(currStockValues.get(i));
            if(abs(currStockValues.get(i) - previousStockValues.get(i)) > 1e-6) {
                return;
            }
        }
    }

    @Then("I should see the time range {word}")
    public void i_should_see_the_time_range(String change) {
        List<WebElement> datepickers = wait10s.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".highcharts-label.highcharts-range-input")));
        LocalDate startDate = LocalDate.parse(processDate(datepickers.get(0).getText()), dateFormat);
        LocalDate endDate = LocalDate.parse(processDate(datepickers.get(1).getText()), dateFormat);
        if (change.equalsIgnoreCase("expands")) {
            // zoom out
            assertTrue(startDate.isBefore(range.startDate) || endDate.isAfter(range.endDate));
        } else if (change.equalsIgnoreCase("shrinks")) {
            // zoom in
            assertTrue(startDate.isAfter(range.startDate) || endDate.isBefore(range.startDate));
        }

        // update
        range.startDate = startDate;
        range.endDate = endDate;
    }
    //#endregion

    //#region Then: security
    @Then("The connection should be refused")
    public void the_connection_should_be_refused() {
        WebElement element = wait10s.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"main-message\"]/h1/span")));
        assertEquals("This page isn’t working", element.getText());
    }

    //#endregion
    @After()
    public void after() throws InterruptedException {
        if (!reuseDriver) {
            Thread.sleep(1000);
            driver.quit();
        }
    }
}
