package csci310.servlets;

import com.google.gson.Gson;
import csci310.Database.DatabaseAPI;
import csci310.model.Stock;
import org.junit.After;
import org.junit.Before;
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
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PortfolioAddStockTest extends Mockito {

    @InjectMocks
    PortfolioAddStock servlet;

    @Spy
    DatabaseAPI spyAPI = DatabaseAPI.getInstance();

    @Mock
    HttpServletRequest mockRequest;

    @Mock
    HttpServletResponse mockResponse;

    @Mock
    PrintWriter mockPw;

    @Mock
    HttpSession mockSession;

    public PortfolioAddStockTest() throws SQLException, ClassNotFoundException {
    }

    @Before
    public void setup() throws SQLException, ClassNotFoundException {
        // Make sure test class default constructor is covered
        new PortfolioAddStock();
    }

    @After
    public void cleanup() throws SQLException {
        spyAPI.clearDB();
    }

    @Test
    public void testDoPostMissingUser() throws IOException {
        // Test data
        String expectedResp = new Gson().toJson(new PortfolioAddStock.Response(
                -1,
                false,
                "Your session has expired. Please log in again.",
                null,
                null,
                null));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostMissingTicker() throws IOException {
        // Test data
        String username = "test_name";
        String quantity = "100";
        String dateBought = "10/10/2020";
        String stockType = "portfolio";
        String expectedResp = new Gson().toJson(new PortfolioAddStock.Response(
                -1,
                false,
                "Ticker field should not be empty",
                null,
                null,
                null));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("ticker")).thenReturn("");
        when(mockRequest.getParameter("quantity")).thenReturn(quantity);
        when(mockRequest.getParameter("dateBought")).thenReturn(dateBought);
        when(mockRequest.getParameter("dateSold")).thenReturn("");
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostInvalidTicker1() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String ticker = "invalid_ticker";
        int quantity = 100;
        String dateBought = "10/01/2020";
        String stockType = "portfolio";
        String expectedResp = new Gson().toJson(new PortfolioAddStock.Response(
                -1,
                false,
                "Ticker should consist of letters only and should not be more than 5 characters long",
                null,
                null,
                null));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("ticker")).thenReturn(ticker);
        when(mockRequest.getParameter("quantity")).thenReturn(Integer.toString(quantity));
        when(mockRequest.getParameter("dateBought")).thenReturn(dateBought);
        when(mockRequest.getParameter("dateSold")).thenReturn("");
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
        // Also make sure the invalid stock has never been added to DB
        verify(spyAPI, never()).addStock(any(), any());
    }

    @Test
    public void testDoPostInvalidTicker2() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String ticker = "asdf";
        int quantity = 100;
        String dateBought = "10/01/2020";
        String stockType = "portfolio";
        String expectedResp = new Gson().toJson(new PortfolioAddStock.Response(
                -1,
                false,
                "Please enter a NASDAQ or NYSE stock ticker",
                null,
                null,
                null));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("ticker")).thenReturn(ticker);
        when(mockRequest.getParameter("quantity")).thenReturn(Integer.toString(quantity));
        when(mockRequest.getParameter("dateBought")).thenReturn(dateBought);
        when(mockRequest.getParameter("dateSold")).thenReturn("");
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
        // Also make sure the invalid stock has never been added to DB
        verify(spyAPI, never()).addStock(any(), any());
    }

    @Test
    public void testDoPostInvalidTicker3() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String ticker = "XIACF";
        int quantity = 100;
        String dateBought = "10/01/2020";
        String stockType = "portfolio";
        String expectedResp = new Gson().toJson(new PortfolioAddStock.Response(
                -1,
                false,
                "Please enter a NASDAQ or NYSE stock ticker",
                null,
                null,
                null));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("ticker")).thenReturn(ticker);
        when(mockRequest.getParameter("quantity")).thenReturn(Integer.toString(quantity));
        when(mockRequest.getParameter("dateBought")).thenReturn(dateBought);
        when(mockRequest.getParameter("dateSold")).thenReturn("");
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
        // Also make sure the invalid stock has never been added to DB
        verify(spyAPI, never()).addStock(any(), any());
    }

    @Test
    public void testDoPostMissingQuantity() throws IOException {
        // Test data
        String username = "test_name";
        String ticker = "aapl";
        String dateBought = "10/01/2020";
        String stockType = "portfolio";
        String expectedResp = new Gson().toJson(new PortfolioAddStock.Response(
                -1,
                false,
                null,
                "Number of shares field should not be empty",
                null,
                null));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("ticker")).thenReturn(ticker);
        when(mockRequest.getParameter("quantity")).thenReturn("");
        when(mockRequest.getParameter("dateBought")).thenReturn(dateBought);
        when(mockRequest.getParameter("dateSold")).thenReturn("");
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostNonPositiveQuantity() throws IOException {
        // Test data
        String username = "test_name";
        String ticker = "aapl";
        String quantity = "-100";
        String dateBought = "10/01/2020";
        String stockType = "portfolio";
        String expectedResp = new Gson().toJson(new PortfolioAddStock.Response(
                -1,
                false,
                null,
                "Number of shares should be a positive integer",
                null,
                null));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("ticker")).thenReturn(ticker);
        when(mockRequest.getParameter("quantity")).thenReturn(quantity);
        when(mockRequest.getParameter("dateBought")).thenReturn(dateBought);
        when(mockRequest.getParameter("dateSold")).thenReturn("");
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostMissingDateBought1() throws IOException {
        // Test data
        String username = "test_name";
        String ticker = "aapl";
        int quantity = 100;
        String dateSold = "10/15/2020";
        String stockType = "portfolio";
        String expectedResp = new Gson().toJson(new PortfolioAddStock.Response(
                -1,
                false,
                null,
                null,
                "Date bought field should not be empty",
                null));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("ticker")).thenReturn(ticker);
        when(mockRequest.getParameter("quantity")).thenReturn(Integer.toString(quantity));
        when(mockRequest.getParameter("dateBought")).thenReturn("");
        when(mockRequest.getParameter("dateSold")).thenReturn(dateSold);
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostMissingDateBought2() throws IOException {
        // Test data
        String username = "test_name";
        String ticker = "aapl";
        int quantity = 100;
        String dateSold = "10/15/2020";
        String stockType = "portfolio";
        String expectedResp = new Gson().toJson(new PortfolioAddStock.Response(
                -1,
                false,
                null,
                null,
                "Date bought field should not be empty",
                null));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("ticker")).thenReturn(ticker);
        when(mockRequest.getParameter("quantity")).thenReturn(Integer.toString(quantity));
        when(mockRequest.getParameter("dateBought")).thenReturn("null");
        when(mockRequest.getParameter("dateSold")).thenReturn(dateSold);
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostInvalidDateBought() throws IOException {
        // Test data
        String username = "test_name";
        String ticker = "aapl";
        int quantity = 100;
        String dateBought = "2020-10-01";
        String stockType = "portfolio";
        String expectedResp = new Gson().toJson(new PortfolioAddStock.Response(
                -1,
                false,
                null,
                null,
                "Please enter a valid date of format MM/DD/YYYY",
                null));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("ticker")).thenReturn(ticker);
        when(mockRequest.getParameter("quantity")).thenReturn(Integer.toString(quantity));
        when(mockRequest.getParameter("dateBought")).thenReturn(dateBought);
        when(mockRequest.getParameter("dateSold")).thenReturn("");
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostInvalidDateSold() throws IOException {
        // Test data
        String username = "test_name";
        String ticker = "aapl";
        int quantity = 100;
        String dateBought = "10/01/2020";
        String dateSold = "2019-10-01";
        String stockType = "portfolio";
        String expectedResp = new Gson().toJson(new PortfolioAddStock.Response(
                -1,
                false,
                null,
                null,
                null,
                "Please enter a valid date of format MM/DD/YYYY"));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("ticker")).thenReturn(ticker);
        when(mockRequest.getParameter("quantity")).thenReturn(Integer.toString(quantity));
        when(mockRequest.getParameter("dateBought")).thenReturn(dateBought);
        when(mockRequest.getParameter("dateSold")).thenReturn(dateSold);
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostDateBoughtLaterThanDateSold() throws IOException {
        // Test data
        String username = "test_name";
        String ticker = "aapl";
        int quantity = 100;
        String dateBought = "10/10/2020";
        String dateSold = "10/01/2020";
        String stockType = "portfolio";
        String expectedResp = new Gson().toJson(new PortfolioAddStock.Response(
                -1,
                false,
                null,
                null,
                "Date bought cannot be later than Date sold",
                "Date sold cannot be earlier than Date bought"));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("ticker")).thenReturn(ticker);
        when(mockRequest.getParameter("quantity")).thenReturn(Integer.toString(quantity));
        when(mockRequest.getParameter("dateBought")).thenReturn(dateBought);
        when(mockRequest.getParameter("dateSold")).thenReturn(dateSold);
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostHistoricalStockRequest() throws IOException, SQLException {
        // Test data
        String userName = "test";
        int stockId = 1;
        String ticker = "aapl";
        int quantity = 100;
        String dateBought = "02/28/2019";
        String dateSold = "10/01/2020";
        String stockType = "historical";
        String expectedResp = new Gson().toJson(new PortfolioAddStock.Response(
                -1, true, new PortfolioAddStock.Message()));

        // Mocking behavior
        when(mockRequest.getParameter("ticker")).thenReturn(ticker);
        when(mockRequest.getParameter("quantity")).thenReturn(Integer.toString(quantity));
        when(mockRequest.getParameter("dateBought")).thenReturn(dateBought);
        when(mockRequest.getParameter("dateSold")).thenReturn(dateSold);
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockResponse.getWriter()).thenReturn(mockPw);
        Mockito.doReturn(stockId).when(spyAPI).addStock(any(), any());

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify that Database api was not called
        verify(spyAPI, times(0)).addUser(any(), any());
        verify(mockPw).print(expectedResp);
        verify(mockResponse).setStatus(200);
    }

    @Test
    public void testDoPostWithDateSold() throws IOException, SQLException {
        // Test data
        String userName = "test";
        int stockId = 1;
        String ticker = "AAPL";
        int quantity = 100;
        String dateBought = "10/01/2010";
        String dateSold = "10/01/2020";
        String dateBoughtContent = "2010-10-01";
        String dateSoldContent = "2020-10-01";
        String stockType = "portfolio";
        String expectedResp = new Gson().toJson(new PortfolioAddStock.Response(
                stockId, true, new PortfolioAddStock.Message()));

        // Mocking behavior
        when(mockRequest.getParameter("ticker")).thenReturn(ticker);
        when(mockRequest.getParameter("quantity")).thenReturn(Integer.toString(quantity));
        when(mockRequest.getParameter("dateBought")).thenReturn(dateBought);
        when(mockRequest.getParameter("dateSold")).thenReturn(dateSold);
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockResponse.getWriter()).thenReturn(mockPw);
        Mockito.doReturn(stockId).when(spyAPI).addStock(any(), any());

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        ArgumentCaptor<Stock> argument = ArgumentCaptor.forClass(Stock.class);
        verify(spyAPI).addStock(eq(userName), argument.capture());
        assertEquals(ticker, argument.getValue().getTicker());
        assertEquals(quantity, argument.getValue().getQuantity());
        assertEquals(dateBoughtContent, argument.getValue().getDateBoughtString());
        assertEquals(dateSoldContent, argument.getValue().getDateSoldString());
        verify(mockPw).print(expectedResp);
        verify(mockResponse).setStatus(200);
    }

    @Test
    public void testDoPostWithDateSoldWithDB() throws IOException, SQLException {
        // Test data
        String userName = "test_name";
        String password = "test_pwd";
        String ticker = "AAPL";
        int quantity = 100;
        String dateBoughtContent = "2010-10-01";
        String dateSoldContent = "2020-10-01";
        String dateBought = "10/01/2010";
        String dateSold = "10/01/2020";
        String stockType = "portfolio";

        // Mocking behavior
        when(mockRequest.getParameter("ticker")).thenReturn(ticker);
        when(mockRequest.getParameter("quantity")).thenReturn(Integer.toString(quantity));
        when(mockRequest.getParameter("dateBought")).thenReturn(dateBought);
        when(mockRequest.getParameter("dateSold")).thenReturn(dateSold);
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Pre-test setup
        // Make sure the user is present
        if (spyAPI.getUser(userName) == null) {
            spyAPI.addUser(userName, password);
        }

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        List<Stock> stockList = spyAPI.getStockList(userName);
        int stockId = -1;
        for (Stock stock : stockList) {
            if (ticker.equals(stock.getTicker()) &&
                    quantity == stock.getQuantity() &&
                    dateBoughtContent.equals(stock.getDateBoughtString()) &&
                    dateSoldContent.equals(stock.getDateSoldString())) {
                stockId = stock.getStockId();
                break;
            }
        }
        assertTrue(stockId >= 0);

        // Cleanup
        spyAPI.deleteStock(userName, stockId);
    }

    @Test
    public void testDoPostWithNoDateSold() throws IOException, SQLException {
        // Test data
        String userName = "test";
        int stockId = 1;
        String ticker = "AA";
        int quantity = 100;
        String dateBoughtContent = "2010-10-01";
        String dateBought = "10/01/2010";
        String stockType = "portfolio";
        String expectedResp = new Gson().toJson(new PortfolioAddStock.Response(
                stockId, true, new PortfolioAddStock.Message()));

        // Mocking behavior
        when(mockRequest.getParameter("ticker")).thenReturn(ticker);
        when(mockRequest.getParameter("quantity")).thenReturn(Integer.toString(quantity));
        when(mockRequest.getParameter("dateBought")).thenReturn(dateBought);
        when(mockRequest.getParameter("dateSold")).thenReturn("null");
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockResponse.getWriter()).thenReturn(mockPw);
        Mockito.doReturn(stockId).when(spyAPI).addStock(any(), any());

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        ArgumentCaptor<Stock> argument = ArgumentCaptor.forClass(Stock.class);
        verify(spyAPI).addStock(eq(userName), argument.capture());
        assertEquals(ticker, argument.getValue().getTicker());
        assertEquals(quantity, argument.getValue().getQuantity());
        assertEquals(dateBoughtContent, argument.getValue().getDateBoughtString());
        assertNull(argument.getValue().getDateSold());
        verify(mockPw).print(expectedResp);
        verify(mockResponse).setStatus(200);
    }

    @Test
    public void testDoPostWithNoDateSoldWithDB() throws IOException, SQLException {
        // Test data
        String userName = "test_name";
        String password = "test_pwd";
        String ticker = "AA";
        int quantity = 100;
        String dateBoughtContent = "2010-10-01";
        String dateBought = "10/01/2010";
        String stockType = "portfolio";
        // Mocking behavior
        when(mockRequest.getParameter("ticker")).thenReturn(ticker);
        when(mockRequest.getParameter("quantity")).thenReturn(Integer.toString(quantity));
        when(mockRequest.getParameter("dateBought")).thenReturn(dateBought);
        when(mockRequest.getParameter("dateSold")).thenReturn("");
        when(mockRequest.getParameter("stockType")).thenReturn(stockType);
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Pre-test setup
        // Make sure the user is present
        if (spyAPI.getUser(userName) == null) {
            spyAPI.addUser(userName, password);
        }

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        List<Stock> stockList = spyAPI.getStockList(userName);
        int stockId = -1;
        for (Stock stock : stockList) {
            if (ticker.equals(stock.getTicker()) &&
                    quantity == stock.getQuantity() &&
                    dateBoughtContent.equals(stock.getDateBoughtString())) {
                stockId = stock.getStockId();
                break;
            }
        }
        assertTrue(stockId >= 0);

        // Clean up DB
        spyAPI.deleteStock(userName, stockId);
    }

    @Test
    public void testDoPostInternalServerError() throws IOException {
        // Mocking behavior
        when(mockRequest.getSession()).thenThrow(Exception.class);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockResponse).setStatus(500);
    }
}