package csci310.servlets;

import com.google.gson.Gson;
import csci310.Database.DatabaseAPI;
import csci310.model.Stock;
import csci310.trading.StockEndpoint;
import csci310.trading.StockTrading;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class PortfolioReturnStockDataTest extends Mockito {

    @InjectMocks
    PortfolioReturnStockData servlet;

    @Spy
    DatabaseAPI spyAPI = DatabaseAPI.getInstance();

    @Spy
    StockTrading spySt = new StockTrading();

    @Mock
    HttpServletRequest mockRequest;

    @Mock
    HttpServletResponse mockResponse;

    @Mock
    HttpSession mockSession;

    @Mock
    PrintWriter mockPw;

    public PortfolioReturnStockDataTest() throws SQLException, ClassNotFoundException {
    }

    @BeforeClass
    public static void setup() throws SQLException, ClassNotFoundException {
        // Make sure test class default constructor is covered
        new PortfolioReturnStockData();
    }

    @Test
    public void testDoPostMissingUser() throws IOException {
        // Test data
        String tickerList = "['aapl']";
        String startDate = "10/10/2020";
        String endDate = "10/15/2020";
        String frequency = "daily";
        String expectedResp = new Gson().toJson(new PortfolioReturnStockData.Response(
                "Your session has expired. Please log in again.",
                null
        ));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockRequest.getParameter("tickerList")).thenReturn(tickerList);
        when(mockRequest.getParameter("startDate")).thenReturn(startDate);
        when(mockRequest.getParameter("endDate")).thenReturn(endDate);
        when(mockRequest.getParameter("frequency")).thenReturn(frequency);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostInvalidStartDate() throws IOException {
        // Test 1
        // Test data
        String username = "test_name";
        String tickerList = "['aapl']";
        String startDate = "2020-10-10";
        String endDate = "10/15/2020";
        String frequency = "daily";
        String expectedResp = new Gson().toJson(new PortfolioReturnStockData.Response(
                "Please enter a valid date of format MM/DD/YYYY",
                null
        ));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("tickerList")).thenReturn(tickerList);
        when(mockRequest.getParameter("startDate")).thenReturn(startDate);
        when(mockRequest.getParameter("endDate")).thenReturn(endDate);
        when(mockRequest.getParameter("frequency")).thenReturn(frequency);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);

        // Test 2
        startDate = "something else";

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw, times(2)).print(expectedResp);
    }

    @Test
    public void testDoPostInvalidEndDate() throws IOException {
        // Test 1
        // Test data
        String username = "test_name";
        String tickerList = "['aapl']";
        String startDate = "10/10/2020";
        String endDate = "2020-10-15";
        String frequency = "daily";
        String expectedResp = new Gson().toJson(new PortfolioReturnStockData.Response(
                null,
                "Please enter a valid date of format MM/DD/YYYY"
        ));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("tickerList")).thenReturn(tickerList);
        when(mockRequest.getParameter("startDate")).thenReturn(startDate);
        when(mockRequest.getParameter("endDate")).thenReturn(endDate);
        when(mockRequest.getParameter("frequency")).thenReturn(frequency);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);

        // Test 2
        startDate = "something else";

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw, times(2)).print(expectedResp);
    }

    @Test
    public void testDoPostDefaultStartDateEmptyStockList() throws IOException, SQLException {
        // Test 1
        // Test data
        String username = "test_name";
        String tickerList = "['aapl']";
        String startDate = "";
        String endDate = "10/15/2020";
        String expectedStartDate = LocalDate.now().minus(3, ChronoUnit.MONTHS).toString();
        String frequency = "daily";
        String stockType = "historical";
        List<Stock> stockList = new ArrayList<>();

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("tickerList")).thenReturn(tickerList);
        when(mockRequest.getParameter("startDate")).thenReturn(startDate);
        when(mockRequest.getParameter("endDate")).thenReturn(endDate);
        when(mockRequest.getParameter("frequency")).thenReturn(frequency);
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        Mockito.doReturn(stockList).when(spyAPI).getStockList(username);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(spySt).getHistoricalPrices(anyString(), eq(expectedStartDate), anyString(), any());
    }

    @Test
    public void testDoPostDefaultStartDateNonemptyStockList() throws IOException, SQLException {
        // Test 1
        // Test data
        String username = "test_name";
        String tickerList = "['aapl']";
        String startDate = "";
        String endDate = "10/15/2020";
        String frequency = "daily";
        String stockType = "historical";
        List<Stock> stockList = new ArrayList<>();
        stockList.add(new Stock("aapl", 100, username, "2019-03-03", "2020-10-10"));
        stockList.add(new Stock("googl", 100, username, "2019-01-01", "2020-10-10"));
        stockList.add(new Stock("aa", 100, username, "2019-05-05", "2020-10-10"));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("tickerList")).thenReturn(tickerList);
        when(mockRequest.getParameter("startDate")).thenReturn(startDate);
        when(mockRequest.getParameter("endDate")).thenReturn(endDate);
        when(mockRequest.getParameter("frequency")).thenReturn(frequency);
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        Mockito.doReturn(stockList).when(spyAPI).getStockList(username);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(spySt).getHistoricalPrices(anyString(), eq("2019-01-01"), anyString(), any());
    }

    @Test
    public void testDoPostDefaultEndDate() throws IOException, SQLException {
        // Test 1
        // Test data
        String username = "test_name";
        String tickerList = "['aapl']";
        String startDate = "10/15/2019";
        String endDate = "";
        String expectedEndDate = LocalDate.now().toString();
        String frequency = "daily";
        String stockType = "historical";
        List<Stock> stockList = new ArrayList<>();

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("tickerList")).thenReturn(tickerList);
        when(mockRequest.getParameter("startDate")).thenReturn(startDate);
        when(mockRequest.getParameter("endDate")).thenReturn(endDate);
        when(mockRequest.getParameter("frequency")).thenReturn(frequency);
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        Mockito.doReturn(stockList).when(spyAPI).getStockList(username);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(spySt).getHistoricalPrices(anyString(), anyString(), eq(expectedEndDate), any());
    }

    @Test
    public void testDoPostStartDateLaterThanEndDate() throws IOException {
        // Test data
        String username = "test_name";
        String tickerList = "['aapl', 'goog', 'tsla']";
        String startDate = "10/15/2020";
        String endDate = "10/01/2020";
        String frequency = "daily";
        String stockType = "historical";

        String expectedResp = new Gson().toJson(new PortfolioReturnStockData.Response(
                "Start date cannot be later than End date",
                "End date cannot be earlier than Start date"));

        // Mock objects
        PrintWriter pw = mock(PrintWriter.class);

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("tickerList")).thenReturn(tickerList);
        when(mockRequest.getParameter("startDate")).thenReturn(startDate);
        when(mockRequest.getParameter("endDate")).thenReturn(endDate);
        when(mockRequest.getParameter("frequency")).thenReturn(frequency);
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockResponse.getWriter()).thenReturn(pw);

        // Placeholder method call and assert
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(pw).print(expectedResp);
    }

    @Test
    public void testDoPostHistoricalData() throws IOException {
        // Test data
        String username = "test_name";
        String tickerList = "['aapl', 'goog', 'tsla']";
        String startDate = "10/01/2020";
        String endDate = "10/15/2020";
        String frequency = "daily";
        String stockType = "historical";

        StockTrading st = new StockTrading();
        Map<String, List<StockEndpoint>> expectedRespObj = new HashMap<>();
        expectedRespObj.put("AAPL", st.getHistoricalPrices("aapl", "2020-10-01", "2020-10-15", StockTrading.ResampleFreq.DAILY));
        expectedRespObj.put("GOOG", st.getHistoricalPrices("goog", "2020-10-01", "2020-10-15", StockTrading.ResampleFreq.DAILY));
        expectedRespObj.put("TSLA", st.getHistoricalPrices("tsla", "2020-10-01", "2020-10-15", StockTrading.ResampleFreq.DAILY));

        String expectedResp = new Gson().toJson(new PortfolioReturnStockData.Response(expectedRespObj));

        // Mock objects
        PrintWriter pw = mock(PrintWriter.class);

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("tickerList")).thenReturn(tickerList);
        when(mockRequest.getParameter("startDate")).thenReturn(startDate);
        when(mockRequest.getParameter("endDate")).thenReturn(endDate);
        when(mockRequest.getParameter("frequency")).thenReturn(frequency);
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockResponse.getWriter()).thenReturn(pw);

        // Placeholder method call and assert
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(pw).print(expectedResp);
    }

    @Test
    public void testDoPostPortfolioAggregatedFakeDataWithDateSold() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String tickerList = "['aapl', 'goog', 'fb']";
        String startDate = "10/01/2020";
        String endDate = "10/05/2020";
        String frequency = "daily";
        String stockType = "portfolio";

        // Stock record
        List<Stock> stockRecord = new ArrayList<>();
        stockRecord.add(new Stock("aapl", 10, username, "2020-10-01", "2020-10-03"));
        stockRecord.add(new Stock("aapl", 50, username, "2020-10-03", "2020-10-04"));
        stockRecord.add(new Stock("goog", 20, username, "2020-10-02", "2020-10-04"));
        stockRecord.add(new Stock("fb", 30, username, "2020-10-03", "2020-10-05"));

        // Fake stock data
        List<StockEndpoint> testData1 = new ArrayList<>();
        testData1.add(new StockEndpoint("2020-10-01T00:00:00.000Z", 1));
        testData1.add(new StockEndpoint("2020-10-02T00:00:00.000Z", 2));
        testData1.add(new StockEndpoint("2020-10-03T00:00:00.000Z", 3));
        testData1.add(new StockEndpoint("2020-10-04T00:00:00.000Z", 4));
        testData1.add(new StockEndpoint("2020-10-05T00:00:00.000Z", 5));

        List<StockEndpoint> testData2 = new ArrayList<>();
        testData2.add(new StockEndpoint("2020-10-01T00:00:00.000Z", 6));
        testData2.add(new StockEndpoint("2020-10-02T00:00:00.000Z", 7));
        testData2.add(new StockEndpoint("2020-10-03T00:00:00.000Z", 8));
        testData2.add(new StockEndpoint("2020-10-04T00:00:00.000Z", 9));
        testData2.add(new StockEndpoint("2020-10-05T00:00:00.000Z", 10));

        List<StockEndpoint> testData3 = new ArrayList<>();
        testData3.add(new StockEndpoint("2020-10-01T00:00:00.000Z", 11));
        testData3.add(new StockEndpoint("2020-10-02T00:00:00.000Z", 12));
        testData3.add(new StockEndpoint("2020-10-03T00:00:00.000Z", 13));
        testData3.add(new StockEndpoint("2020-10-04T00:00:00.000Z", 14));
        testData3.add(new StockEndpoint("2020-10-05T00:00:00.000Z", 15));

        List<StockEndpoint> aggregated = new ArrayList<>();
        aggregated.add(new StockEndpoint("2020-10-01T00:00:00.000Z", 10));
        aggregated.add(new StockEndpoint("2020-10-02T00:00:00.000Z", 160));
        aggregated.add(new StockEndpoint("2020-10-03T00:00:00.000Z", 700));
        aggregated.add(new StockEndpoint("2020-10-04T00:00:00.000Z", 420));
        aggregated.add(new StockEndpoint("2020-10-05T00:00:00.000Z", 0));
        Map<String, List<StockEndpoint>> expectedRespObj = new HashMap<>();
        expectedRespObj.put("aggregated", aggregated);
        String expectedResp = new Gson().toJson(new PortfolioReturnStockData.Response(expectedRespObj));

        // Mock objects
        PrintWriter pw = mock(PrintWriter.class);

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("tickerList")).thenReturn(tickerList);
        when(mockRequest.getParameter("startDate")).thenReturn(startDate);
        when(mockRequest.getParameter("endDate")).thenReturn(endDate);
        when(mockRequest.getParameter("frequency")).thenReturn(frequency);
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);

        // Return mock stock record when queried
        Mockito.doReturn(stockRecord).when(spyAPI).getStockList(username);

        // Return mock stock data when queried
        Mockito.doReturn(testData1).when(spySt).getHistoricalPrices(eq("aapl"), anyString(), anyString(), any());
        Mockito.doReturn(testData2).when(spySt).getHistoricalPrices(eq("goog"), anyString(), anyString(), any());
        Mockito.doReturn(testData3).when(spySt).getHistoricalPrices(eq("fb"), anyString(), anyString(), any());

        when(mockResponse.getWriter()).thenReturn(pw);

        // Placeholder method call and assert
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(pw).print(expectedResp);
    }

    @Test
    public void testDoPostPortfolioAggregatedFakeDataWithNoDateSold() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String tickerList = "['aapl', 'goog', 'fb']";
        String startDate = "10/01/2020";
        String endDate = "10/05/2020";
        String frequency = "daily";
        String stockType = "portfolio";

        // Stock record
        List<Stock> stockRecord = new ArrayList<>();
        stockRecord.add(new Stock("aapl", 10, username, "2020-10-01"));
        stockRecord.add(new Stock("aapl", 50, username, "2020-10-03"));
        stockRecord.add(new Stock("goog", 20, username, "2020-10-02"));
        stockRecord.add(new Stock("fb", 30, username, "2020-10-03"));

        // Fake stock data
        List<StockEndpoint> testData1 = new ArrayList<>();
        testData1.add(new StockEndpoint("2020-10-01T00:00:00.000Z", 1));
        testData1.add(new StockEndpoint("2020-10-02T00:00:00.000Z", 2));
        testData1.add(new StockEndpoint("2020-10-03T00:00:00.000Z", 3));
        testData1.add(new StockEndpoint("2020-10-04T00:00:00.000Z", 4));
        testData1.add(new StockEndpoint("2020-10-05T00:00:00.000Z", 5));

        List<StockEndpoint> testData2 = new ArrayList<>();
        testData2.add(new StockEndpoint("2020-10-01T00:00:00.000Z", 6));
        testData2.add(new StockEndpoint("2020-10-02T00:00:00.000Z", 7));
        testData2.add(new StockEndpoint("2020-10-03T00:00:00.000Z", 8));
        testData2.add(new StockEndpoint("2020-10-04T00:00:00.000Z", 9));
        testData2.add(new StockEndpoint("2020-10-05T00:00:00.000Z", 10));

        List<StockEndpoint> testData3 = new ArrayList<>();
        testData3.add(new StockEndpoint("2020-10-01T00:00:00.000Z", 11));
        testData3.add(new StockEndpoint("2020-10-02T00:00:00.000Z", 12));
        testData3.add(new StockEndpoint("2020-10-03T00:00:00.000Z", 13));
        testData3.add(new StockEndpoint("2020-10-04T00:00:00.000Z", 14));
        testData3.add(new StockEndpoint("2020-10-05T00:00:00.000Z", 15));

        // Expected Data line
        List<StockEndpoint> aggregated = new ArrayList<>();
        aggregated.add(new StockEndpoint("2020-10-01T00:00:00.000Z", 10));
        aggregated.add(new StockEndpoint("2020-10-02T00:00:00.000Z", 160));
        aggregated.add(new StockEndpoint("2020-10-03T00:00:00.000Z", 730));
        aggregated.add(new StockEndpoint("2020-10-04T00:00:00.000Z", 840));
        aggregated.add(new StockEndpoint("2020-10-05T00:00:00.000Z", 950));
        Map<String, List<StockEndpoint>> expectedRespObj = new HashMap<>();
        expectedRespObj.put("aggregated", aggregated);
        String expectedResp = new Gson().toJson(new PortfolioReturnStockData.Response(expectedRespObj));

        // Mock objects
        PrintWriter pw = mock(PrintWriter.class);

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("tickerList")).thenReturn(tickerList);
        when(mockRequest.getParameter("startDate")).thenReturn(startDate);
        when(mockRequest.getParameter("endDate")).thenReturn(endDate);
        when(mockRequest.getParameter("frequency")).thenReturn(frequency);
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);

        // Return mock stock record when queried
        Mockito.doReturn(stockRecord).when(spyAPI).getStockList(username);

        // Return mock stock data when queried
        Mockito.doReturn(testData1).when(spySt).getHistoricalPrices(eq("aapl"), anyString(), anyString(), any());
        Mockito.doReturn(testData2).when(spySt).getHistoricalPrices(eq("goog"), anyString(), anyString(), any());
        Mockito.doReturn(testData3).when(spySt).getHistoricalPrices(eq("fb"), anyString(), anyString(), any());

        when(mockResponse.getWriter()).thenReturn(pw);

        // Placeholder method call and assert
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(pw).print(expectedResp);
    }

    @Test
    public void testDoPostStartDateNotTradingDate() throws IOException {
        // Test data
        String username = "test_name";
        String tickerList = "['aapl', 'goog', 'tsla']";
        String startDate = "10/03/2020";    // This is a Saturday
        String endDate = "10/15/2020";
        String frequency = "daily";
        String stockType = "historical";

        StockTrading st = new StockTrading();
        Map<String, List<StockEndpoint>> expectedRespObj = new HashMap<>();
        expectedRespObj.put("AAPL", st.getHistoricalPrices("aapl", "2020-10-02", "2020-10-15", StockTrading.ResampleFreq.DAILY));
        expectedRespObj.put("GOOG", st.getHistoricalPrices("goog", "2020-10-02", "2020-10-15", StockTrading.ResampleFreq.DAILY));
        expectedRespObj.put("TSLA", st.getHistoricalPrices("tsla", "2020-10-02", "2020-10-15", StockTrading.ResampleFreq.DAILY));

        String expectedResp = new Gson().toJson(new PortfolioReturnStockData.Response(expectedRespObj));

        // Mock objects
        PrintWriter pw = mock(PrintWriter.class);

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("tickerList")).thenReturn(tickerList);
        when(mockRequest.getParameter("startDate")).thenReturn(startDate);
        when(mockRequest.getParameter("endDate")).thenReturn(endDate);
        when(mockRequest.getParameter("frequency")).thenReturn(frequency);
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockResponse.getWriter()).thenReturn(pw);

        // Placeholder method call and assert
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(pw).print(expectedResp);
    }

    @Test
    public void testDoPostInternalServerError() throws IOException {
        // Mocking behavior
        when(mockRequest.getParameter(any())).thenThrow(Exception.class);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockResponse).setStatus(500);
    }
}