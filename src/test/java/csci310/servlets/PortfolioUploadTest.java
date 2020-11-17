package csci310.servlets;

import com.google.gson.Gson;
import csci310.Database.DatabaseAPI;
import csci310.trading.StockTrading;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockPart;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

@RunWith(MockitoJUnitRunner.class)
public class PortfolioUploadTest extends Mockito {

    private final static String testFileDir = "src/test/resources/test_csv/";

    @InjectMocks
    PortfolioUpload servlet;

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

    @Before
    public void setup() throws SQLException, ClassNotFoundException {
        // Make sure test class default constructor is covered
        new PortfolioUpload();
    }

    public PortfolioUploadTest() throws SQLException, ClassNotFoundException {
    }

    @Test
    public void testDoPostMissingUser() throws IOException, ServletException {
        // Test data
        String expectedResp = new Gson().toJson(new PortfolioUpload.Response(
                false,
                "Your session has expired. Please log in again."));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostWrongContentType1() throws IOException, ServletException {
        // Test data
        String username = "test_name";
        String filename = "wrong_content_type_1.json";
        Path filePath = Paths.get(testFileDir.concat(filename));
        MockPart mockPart = new MockPart("file", filename, Files.readAllBytes(filePath));

        String expectedResp = new Gson().toJson(new PortfolioUpload.Response(
                false,
                "Please upload a CSV file"
        ));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getPart("file")).thenReturn(mockPart);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostWrongHeaders1() throws IOException, ServletException {
        // Test data
        String username = "test_name";
        String filename = "wrong_headers_1.csv";
        Path filePath = Paths.get(testFileDir.concat(filename));
        MockPart mockPart = new MockPart("file", filename, Files.readAllBytes(filePath));

        String expectedResp = new Gson().toJson(new PortfolioUpload.Response(
                false,
                "Please make sure the CSV's field names are correct: ticker, quantity, dateBought, dateSold"
        ));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getPart("file")).thenReturn(mockPart);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostWrongMissingData1() throws IOException, ServletException {
        // Test data
        String username = "test_name";
        String filename = "wrong_missing_data_1.csv";
        Path filePath = Paths.get(testFileDir.concat(filename));
        MockPart mockPart = new MockPart("file", filename, Files.readAllBytes(filePath));

        String expectedResp = new Gson().toJson(new PortfolioUpload.Response(
                false,
                "Some data in the CSV file are missing."));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getPart("file")).thenReturn(mockPart);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostWrongInvalidTicker1() throws IOException, ServletException {
        // Test data
        String username = "test_name";
        String filename = "wrong_invalid_ticker_1.csv";
        Path filePath = Paths.get(testFileDir.concat(filename));
        MockPart mockPart = new MockPart("file", filename, Files.readAllBytes(filePath));

        String expectedResp = new Gson().toJson(new PortfolioUpload.Response(
                false,
                "Found invalid ticker. Please check if all tickers are valid " +
                        "NASDAQ or NYSE stock tickers."));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getPart("file")).thenReturn(mockPart);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostWrongInvalidQuantity1() throws IOException, ServletException {
        // Test data
        String username = "test_name";
        String filename = "wrong_invalid_quantity_1.csv";
        Path filePath = Paths.get(testFileDir.concat(filename));
        MockPart mockPart = new MockPart("file", filename, Files.readAllBytes(filePath));

        String expectedResp = new Gson().toJson(new PortfolioUpload.Response(
                false,
                "Found non-positive quantity or malformed quantity value. " +
                        "Please check that all quantity fields are positive integers."));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getPart("file")).thenReturn(mockPart);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostWrongInvalidDate1() throws IOException, ServletException {
        // Test data
        String username = "test_name";
        String filename = "wrong_invalid_date_1.csv";
        Path filePath = Paths.get(testFileDir.concat(filename));
        MockPart mockPart = new MockPart("file", filename, Files.readAllBytes(filePath));

        String expectedResp = new Gson().toJson(new PortfolioUpload.Response(
                false,
                "Cannot parse some of the date values. " +
                        "Please check that all dateBought and dateSold fields are valid date " +
                        "of format MM/DD/YYYY."));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getPart("file")).thenReturn(mockPart);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostWrongDateBoughtLaterThanDateSold1() throws IOException, ServletException {
        // Test data
        String username = "test_name";
        String filename = "wrong_date_bought_later_than_date_sold_1.csv";
        Path filePath = Paths.get(testFileDir.concat(filename));
        MockPart mockPart = new MockPart("file", filename, Files.readAllBytes(filePath));

        String expectedResp = new Gson().toJson(new PortfolioUpload.Response(
                false,
                "Find dateBought later than dateSold. " +
                        "Please check for each entry, dateBought is no later than dateSold."));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getPart("file")).thenReturn(mockPart);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostSuccessfulOperation() throws IOException, ServletException, SQLException {
        // Test data
        String username = "test_name";
        String filename = "correct_1.csv";
        Path filePath = Paths.get(testFileDir.concat(filename));
        MockPart mockPart = new MockPart("file", filename, Files.readAllBytes(filePath));

        String expectedResp = new Gson().toJson(new PortfolioUpload.Response(
                true,
                ""));

        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(username);
        when(mockRequest.getPart("file")).thenReturn(mockPart);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostInternalServerError() throws IOException, ServletException {
        // Mocking behavior
        when(mockRequest.getSession()).thenThrow(Exception.class);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockResponse).setStatus(500);
    }
}