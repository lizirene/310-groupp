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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PortfolioReturnValueAndIncreaseTest extends Mockito {

    @InjectMocks
    @Spy
    PortfolioReturnValueAndIncrease servlet;

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

    public PortfolioReturnValueAndIncreaseTest() throws SQLException, ClassNotFoundException {
    }

    @Before
    public void setup() throws SQLException, ClassNotFoundException {
        // Make sure test class default constructor is covered
        new PortfolioReturnValueAndIncrease();
    }

    @After
    public void cleanup() throws SQLException {
        spyAPI.clearDB();
    }

    @Test
    public void testGetCurrentDate() {
        LocalDate result = servlet.getCurrentDate();
        assertEquals(0, result.compareTo(LocalDate.now()));
    }

    @Test
    public void testDoPostMissingUser() throws IOException {
        // Test data
        String expectedResp = new Gson().toJson(new PortfolioReturnValueAndIncrease.Response(
                false,
                "Cannot identify user from HTTPSession. Please check if user has logged in and a " +
                        "valid session is present",
                null,
                0,
                0));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostNoStock() throws IOException, SQLException {
        // Test data
        String userName = "test";
        List<Stock> stockList = new ArrayList<>();  // Empty list
        String expectedResp = new Gson().toJson(new PortfolioReturnValueAndIncrease.Response(
                true, "", userName, 0, 0));

        // Mock objects
        PrintWriter mockPw = mock(PrintWriter.class);

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockResponse.getWriter()).thenReturn(mockPw);
        Mockito.doReturn(stockList).when(spyAPI).getStockList(userName);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostStockDate_1() throws IOException, SQLException {
        // Test: stock bought today and not sold yet
        // Test data
        String userName = "test";
        List<Stock> stockList = new ArrayList<>();
        stockList.add(new Stock("aapl", 10, userName, "2020-10-15"));

        LocalDate currentDate = LocalDate.parse("2020-10-15");

        // Mock objects
        PrintWriter mockPw = mock(PrintWriter.class);

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockResponse.getWriter()).thenReturn(mockPw);
        Mockito.doReturn(stockList).when(spyAPI).getStockList(userName);
        Mockito.doReturn(currentDate).when(servlet).getCurrentDate();

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        // Capture the printed response
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockPw).print(captor.capture());
        PortfolioReturnValueAndIncrease.Response respObj = new Gson().fromJson(captor.getValue(),
                PortfolioReturnValueAndIncrease.Response.class);
        assertTrue(respObj.increase > 0.0);
    }

    @Test
    public void testDoPostStockDate_2() throws IOException, SQLException {
        // Test: stock bought way ago and sold today
        // Test data
        String userName = "test";
        List<Stock> stockList = new ArrayList<>();
        stockList.add(new Stock("aapl", 10, userName, "2020-10-10", "2020-10-15"));

        LocalDate currentDate = LocalDate.parse("2020-10-15");

        // Mock objects
        PrintWriter mockPw = mock(PrintWriter.class);

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockResponse.getWriter()).thenReturn(mockPw);
        Mockito.doReturn(stockList).when(spyAPI).getStockList(userName);
        Mockito.doReturn(currentDate).when(servlet).getCurrentDate();

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        // Capture the printed response
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockPw).print(captor.capture());
        PortfolioReturnValueAndIncrease.Response respObj = new Gson().fromJson(captor.getValue(),
                PortfolioReturnValueAndIncrease.Response.class);
        assertTrue(respObj.increase < 0.0);
    }

    @Test
    public void testDoPostStockDate_3() throws IOException, SQLException {
        // Test: stock bought today and sold today
        // Test data
        String userName = "test";
        List<Stock> stockList = new ArrayList<>();
        stockList.add(new Stock("aapl", 10, userName, "2020-10-15", "2020-10-15"));

        LocalDate currentDate = LocalDate.parse("2020-10-15");

        // Mock objects
        PrintWriter mockPw = mock(PrintWriter.class);

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockResponse.getWriter()).thenReturn(mockPw);
        Mockito.doReturn(stockList).when(spyAPI).getStockList(userName);
        Mockito.doReturn(currentDate).when(servlet).getCurrentDate();

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        // Capture the printed response
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockPw).print(captor.capture());
        PortfolioReturnValueAndIncrease.Response respObj = new Gson().fromJson(captor.getValue(),
                PortfolioReturnValueAndIncrease.Response.class);
        assertEquals(0.0, respObj.increase, 0.0);
    }

    @Test
    public void testDoPostStockDate_4() throws IOException, SQLException {
        // Test: stock bought way ago and sold way ago
        // Test data
        String userName = "test";
        List<Stock> stockList = new ArrayList<>();
        stockList.add(new Stock("aapl", 10, userName, "2020-10-10", "2020-10-13"));

        LocalDate currentDate = LocalDate.parse("2020-10-15");

        // Mock objects
        PrintWriter mockPw = mock(PrintWriter.class);

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockResponse.getWriter()).thenReturn(mockPw);
        Mockito.doReturn(stockList).when(spyAPI).getStockList(userName);
        Mockito.doReturn(currentDate).when(servlet).getCurrentDate();

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        // Capture the printed response
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockPw).print(captor.capture());
        PortfolioReturnValueAndIncrease.Response respObj = new Gson().fromJson(captor.getValue(),
                PortfolioReturnValueAndIncrease.Response.class);
        assertEquals(0.0, respObj.increase, 0.0);
    }

    @Test
    public void testDoPostStockDate_5() throws IOException, SQLException {
        // Test: (edge case) stock bought tomorrow and sold tomorrow
        // Test data
        String userName = "test";
        List<Stock> stockList = new ArrayList<>();
        stockList.add(new Stock("aapl", 10, userName, "2020-10-16", "2020-10-16"));

        LocalDate currentDate = LocalDate.parse("2020-10-15");

        // Mock objects
        PrintWriter mockPw = mock(PrintWriter.class);

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockResponse.getWriter()).thenReturn(mockPw);
        Mockito.doReturn(stockList).when(spyAPI).getStockList(userName);
        Mockito.doReturn(currentDate).when(servlet).getCurrentDate();

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        // Capture the printed response
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockPw).print(captor.capture());
        PortfolioReturnValueAndIncrease.Response respObj = new Gson().fromJson(captor.getValue(),
                PortfolioReturnValueAndIncrease.Response.class);
        assertEquals(0.0, respObj.increase, 0.0);
    }

    @Test
    public void testDoPostStockDate_6() throws IOException, SQLException {
        // Test: (edge case) stock bought today and sold tomorrow
        // Test data
        String userName = "test";
        List<Stock> stockList = new ArrayList<>();
        stockList.add(new Stock("aapl", 10, userName, "2020-10-15", "2020-10-16"));

        LocalDate currentDate = LocalDate.parse("2020-10-15");

        // Mock objects
        PrintWriter mockPw = mock(PrintWriter.class);

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockResponse.getWriter()).thenReturn(mockPw);
        Mockito.doReturn(stockList).when(spyAPI).getStockList(userName);
        Mockito.doReturn(currentDate).when(servlet).getCurrentDate();

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        // Capture the printed response
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockPw).print(captor.capture());
        PortfolioReturnValueAndIncrease.Response respObj = new Gson().fromJson(captor.getValue(),
                PortfolioReturnValueAndIncrease.Response.class);
//        assertEquals(0.0, respObj.increase, 0.0);
        assertTrue(respObj.increase > 0);
    }

    @Test
    public void testDoPostConsecutiveTradingDate() throws IOException, SQLException {
        // Test data
        String userName = "test";
        List<Stock> stockList = new ArrayList<>();
        stockList.add(new Stock("aapl", 10, userName, "2020-01-01"));
        stockList.add(new Stock("googl", 20, userName, "2020-02-02"));
        stockList.add(new Stock("tsla", 30, userName, "2020-03-03"));

        LocalDate currentDate = LocalDate.parse("2020-10-15"); // Take a regular Thursday

        // Mock objects
        PrintWriter mockPw = mock(PrintWriter.class);

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(userName);
        when(mockResponse.getWriter()).thenReturn(mockPw);
        Mockito.doReturn(stockList).when(spyAPI).getStockList(userName);
        Mockito.doReturn(currentDate).when(servlet).getCurrentDate();

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        // Capture the printed response
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockPw).print(captor.capture());
        PortfolioReturnValueAndIncrease.Response respObj = new Gson().fromJson(captor.getValue(),
                PortfolioReturnValueAndIncrease.Response.class);
        assertNotEquals(0.0, respObj.increase);
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