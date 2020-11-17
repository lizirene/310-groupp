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
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PortfolioReturnStockListTest extends Mockito {

    @InjectMocks
    PortfolioReturnStockList servlet;

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

    public PortfolioReturnStockListTest() throws SQLException, ClassNotFoundException {
    }

    @Before
    public void setup() throws SQLException, ClassNotFoundException {
        // Make sure test class default constructor is covered
        new PortfolioReturnStockList();
    }

    @After
    public void cleanup() throws SQLException {
        spyAPI.clearDB();
    }

    // Util method
    boolean stockExists(String username, String ticker) throws SQLException {
        if (spyAPI.getUser(username) == null) {
            return false;
        }
        List<Stock> stockList = spyAPI.getStockList(username);
        boolean found = false;
        for (Stock stock : stockList) {
            if (ticker.equals(stock.getTicker())) {
                found = true;
            }
        }
        return found;
    }

    @Test
    public void testDoPostMissingUser() throws IOException {
        // Test data
        String expectedResp = new Gson().toJson(new PortfolioReturnStockList.Response(false,
                "Cannot identify user from HTTPSession. Please check if user has logged in and a " +
                        "valid session is present", null));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostCorrectResponse() throws IOException, SQLException {
        // Test data
        String userName = "test";
        List<Stock> stockList = new ArrayList<>();
        stockList.add(new Stock("aapl", 10, userName, "2020-01-01", "2020-10-01"));
        stockList.add(new Stock("googl", 20, userName, "2020-02-02"));
        stockList.add(new Stock("tsla", 30, userName, "2020-03-03"));

        String exptextedResp = new Gson().toJson(new PortfolioReturnStockList.Response(
                true, "", stockList
        ));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute(anyString())).thenReturn("test");
        Mockito.doReturn(stockList).when(spyAPI).getStockList(userName);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Method call
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(exptextedResp);
    }

    @Test
    public void testDoPostCorrectResponseWithDB() throws IOException, SQLException {
        // Test data
        String userName = "test_name";
        String password = "test_pwd";
        List<Stock> stockList = new ArrayList<>();
        stockList.add(new Stock("aapl", 10, userName, "2020-01-01", "2020-10-01"));
        stockList.add(new Stock("googl", 20, userName, "2020-02-02"));
        stockList.add(new Stock("tsla", 30, userName, "2020-03-03"));

        // Pre-test setup
        // Make sure the user is present
        if (spyAPI.getUser(userName) == null) {
            spyAPI.addUser(userName, password);
        }
        // Make sure stock is present
        List<Integer> stockIdList = new ArrayList<>();
        for (Stock stock : stockList) {
            if (!stockExists(userName, stock.getTicker())) {
                int stockId = spyAPI.addStock(userName, stock);
                stockIdList.add(stockId);
                stock.setStockId(stockId);
            }
        }

        String expectedResp = new Gson().toJson(new PortfolioReturnStockList.Response(
                true, "", stockList
        ));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute(anyString())).thenReturn(userName);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Method call
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);

        // Cleanup
        for (Integer stockId : stockIdList) {
            spyAPI.deleteStock(userName, stockId);
        }
    }

    @Test
    public void testDoPostSQLException() throws IOException, SQLException {
        // Test data
        String userName = "test";
        String expectedResp = new Gson().toJson(new PortfolioReturnStockList.Response(false,
                "SQL Exception", null));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockResponse.getWriter()).thenReturn(mockPw);
        Mockito.doThrow(SQLException.class).when(spyAPI).getStockList(userName);

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