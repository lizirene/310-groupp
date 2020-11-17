package csci310.servlets;

import com.google.gson.Gson;
import csci310.Database.DatabaseAPI;
import csci310.model.Stock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
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
public class PortfolioRemoveStockTest extends Mockito {

    @InjectMocks
    PortfolioRemoveStock servlet;

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

    public PortfolioRemoveStockTest() throws SQLException, ClassNotFoundException {
    }

    @Before
    public void setup() throws SQLException, ClassNotFoundException {
        // Make sure test class default constructor is covered
        new PortfolioRemoveStock();
    }

    @After
    public void cleanup() throws SQLException {
        spyAPI.clearDB();
    }

    @Test
    public void testDoPostMissingUser() throws IOException {
        // Test data
        String expectedResp = new Gson().toJson(new PortfolioRemoveStock.Response(false,
                "Cannot identify user from HTTPSession. Please check if user has logged in and a " +
                        "valid session is present"));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostMissingStockId() throws IOException {
        // Test data
        String username = "test_name";
        String expectedResp = new Gson().toJson(new PortfolioRemoveStock.Response(false,
                "Cannot identify 'stockId' field in the POST form."));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostInvalidStockId() throws IOException {
        // Test data
        String stockId = "I am some invalid ID 233333";
        String username = "test_name";
        String expectedResp = new Gson().toJson(new PortfolioRemoveStock.Response(false,
                "Cannot identify 'stockId' field in the POST form."));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getParameter("stockId")).thenReturn(stockId);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostSuccessfulOperation() throws IOException, SQLException {
        // Test data
        int stockId = 1;
        String userName = "test";
        String expectedResp = new Gson().toJson(new PortfolioRemoveStock.Response(true, ""));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockRequest.getParameter("stockId")).thenReturn(Integer.toString(stockId));
        when(mockResponse.getWriter()).thenReturn(mockPw);
        Mockito.doReturn(true).when(spyAPI).deleteStock(userName, stockId);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostSuccessfulOperationWithDB() throws IOException, SQLException {
        // Test data
        String userName = "test_name";
        String password = "test_pwd";
        String ticker = "aapl";
        int quantity = 100;
        String dateBought = "2020-10-01";
        String dateSold = "2020-10-15";
        Stock expectedStock = new Stock(ticker, quantity, userName, dateBought, dateSold);

        // Pre-test setup
        // Make sure the user is present
        if (spyAPI.getUser(userName) == null) {
            spyAPI.addUser(userName, password);
        }
        // Make sure stock is present
        List<Stock> stockList = spyAPI.getStockList(userName);
        int stockId = -1;
        for (Stock stock : stockList) {
            if (ticker.equals(stock.getTicker()) &&
                    quantity == stock.getQuantity() &&
                    dateBought.equals(stock.getDateBought()) &&
                    dateSold.equals(stock.getDateSold())) {
                stockId = stock.getStockId();
                break;
            }
        }
        if (stockId < 0) {
            stockId = spyAPI.addStock(userName, expectedStock);
        }

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockRequest.getParameter("stockId")).thenReturn(Integer.toString(stockId));
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        stockList = spyAPI.getStockList(userName);
        boolean found = false;
        for (Stock stock : stockList) {
            if (ticker.equals(stock.getTicker()) &&
                    quantity == stock.getQuantity() &&
                    dateBought.equals(stock.getDateBought()) &&
                    dateSold.equals(stock.getDateSold())) {
                found = true;
                break;
            }
        }
        assertFalse(found);
    }

    @Test
    public void testDoPostSQLException() throws IOException, SQLException {
        // Test data
        int stockId = 1;
        String userName = "test";
        String expectedResp = new Gson().toJson(new PortfolioRemoveStock.Response(false,
                "SQL Exception"));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockRequest.getParameter("stockId")).thenReturn(Integer.toString(stockId));
        when(mockResponse.getWriter()).thenReturn(mockPw);
        Mockito.doThrow(SQLException.class).when(spyAPI).deleteStock(userName, stockId);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
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