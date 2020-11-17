package csci310.servlets;

import com.google.gson.Gson;
import csci310.Database.DatabaseAPI;
import csci310.model.User;
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
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@RunWith(MockitoJUnitRunner.class)
public class SignupServletTest extends Mockito {

    @InjectMocks
    SignupServlet servlet;

    @Spy
    DatabaseAPI spyAPI = DatabaseAPI.getInstance();

    @Mock
    HttpServletRequest mockRequest;

    @Mock
    HttpServletResponse mockResponse;

    @Mock
    PrintWriter mockPw;

    public SignupServletTest() throws SQLException, ClassNotFoundException {
    }

    @Before
    public void setup() throws SQLException, ClassNotFoundException {
        // Just to make sure the SigninServlet's default constructor is covered
        new SignupServlet();
    }

    @After
    public void cleanup() throws SQLException {
        spyAPI.clearDB();
    }

    @Test
    public void testDoPostMalformedUsername() throws IOException {
        // Test data
        String username = "I am invalid!!!!";
        String password = "test_pwd";
        String repeatPassword = "test_pwd";
        String expectedResp = new Gson().toJson(new SignupServlet.Response(
                false,
                "Username should be between 3 and 20 characters and only contain alphanumeric characters or underscore",
                null,
                null
        ));

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        when(mockRequest.getParameter("repeatPassword")).thenReturn(repeatPassword);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostAccountAlreadyExists() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "test_pwd";
        String repeatPassword = "test_pwd";
        User user = new User(username, password);
        String expectedResp = new Gson().toJson(new SignupServlet.Response(
                false,
                "An account associated with that username already exists",
                null,
                null));

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        when(mockRequest.getParameter("repeatPassword")).thenReturn(repeatPassword);
        Mockito.doReturn(user).when(spyAPI).getUser(username);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostMalformedPasswordTooLong() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "THIS PASSWORD IS WAAAAAAAYYYYYYYY TOOOOOOOO LOOOOOOONG";
        String repeatPassword = "THIS PASSWORD IS WAAAAAAAYYYYYYYY TOOOOOOOO LOOOOOOONG";
        String expectedResp = new Gson().toJson(new SignupServlet.Response(
                false,
                null,
                "Password should be between 6 and 20 characters",
                null
        ));

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        when(mockRequest.getParameter("repeatPassword")).thenReturn(repeatPassword);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostMalformedPasswordTooShort() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "A";
        String repeatPassword = "A";
        String expectedResp = new Gson().toJson(new SignupServlet.Response(
                false,
                null,
                "Password should be between 6 and 20 characters",
                null
        ));

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        when(mockRequest.getParameter("repeatPassword")).thenReturn(repeatPassword);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostDifferentRepeatPassword() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "test_pwd";
        String repeatPassword = "test_pwd_1";
        String expectedResp = new Gson().toJson(new SignupServlet.Response(
                false,
                null,
                null,
                "The password does not match"
        ));

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        when(mockRequest.getParameter("repeatPassword")).thenReturn(repeatPassword);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostAccountAlreadyExistsWithDB() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "test_pwd";
        String repeatPassword = "test_pwd";
        new User(username, password);
        String expectedResp = new Gson().toJson(new SignupServlet.Response(
                false,
                "An account associated with that username already exists",
                null,
                null));

        // Pre-test: Make sure the user already exists
        if (spyAPI.getUser(username) == null) {
            spyAPI.addUser(username, password);
        }

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        when(mockRequest.getParameter("repeatPassword")).thenReturn(repeatPassword);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostSuccessfulOperation() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "test_pwd";
        String repeatPassword = "test_pwd";
        String expectedResp = new Gson().toJson(new SignupServlet.Response(
                true, new SignupServlet.Message()));

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        when(mockRequest.getParameter("repeatPassword")).thenReturn(repeatPassword);
        Mockito.doReturn(null).when(spyAPI).getUser(username);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostSuccessfulOperationWithDB() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "test_pwd";
        String repeatPassword = "test_pwd";
        String expectedResp = new Gson().toJson(new SignupServlet.Response(
                true, new SignupServlet.Message()));

        // Pre-test: Make sure user is not present, otherwise change username
        while (spyAPI.getUser(username) != null) {
            username += "_1";
        }

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        when(mockRequest.getParameter("repeatPassword")).thenReturn(repeatPassword);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }


    @Test
    public void testDoPostInternalServerError() throws IOException {
        // Mocking behavior
        when(mockRequest.getParameter(anyString())).thenThrow(Exception.class);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockResponse).setStatus(500);
    }
}
