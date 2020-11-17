package csci310.servlets;

import com.google.gson.Gson;
import csci310.Database.DatabaseAPI;
import csci310.model.User;
import csci310.utils.UserLock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.AdditionalMatchers.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class SigninServletTest extends Mockito {

    @InjectMocks
    SigninServlet servlet;

	@Spy
    DatabaseAPI spyAPI = DatabaseAPI.getInstance();

    @Mock
    HttpServletRequest mockRequest;

    @Mock
    HttpServletResponse mockResponse;

    @Mock
    HttpSession mockSession;

    @Spy
    Map<String, UserLock> attemptLocks = new HashMap<>();

    @Spy
    UserLock spyLock = new UserLock(false);

    @Mock
    PrintWriter mockPw;

    Instant fromDateString(String date) {
        return LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public SigninServletTest() throws SQLException, ClassNotFoundException {
    }

    @Before
	public void setup() throws SQLException, ClassNotFoundException {
		// Just to make sure the SigninServlet's default constructor is covered
		new SigninServlet();
	}

    @After
    public void cleanup() throws SQLException {
        spyAPI.clearDB();
    }

    @Test
    public void testDoPostUserNotFound() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "test_pwd";
        String expectedResp = new Gson().toJson(new SigninServlet.Response(
                false,
                "No account was found.",
                null));

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        Mockito.doReturn(null).when(spyAPI).getUser(anyString());
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostUserNotFoundWithDB() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "test_pwd";
        String expectedResp = new Gson().toJson(new SigninServlet.Response(
                false,
                "No account was found.",
                null));

        // Pre-test: Make sure user is not present, otherwise change username
        while (spyAPI.getUser(username) != null) {
            username += "_1";
        }

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostAccountLocked() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "test_pwd";
        User user = new User(username, password);
        attemptLocks.put(username, spyLock);
        String expectedResp = new Gson().toJson(new SigninServlet.Response(
                false,
                "You have made 3 failed login attempts in the past minute. " +
                        "Please try again a minute later.",
                null));

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        Mockito.doReturn(user).when(spyAPI).getUser(username);
        // Mock the lock so that isLocked returns true
        Mockito.doReturn(true).when(spyLock).isLocked();
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);

        // Cleanup
        attemptLocks.clear();
    }

    @Test
    public void testDoPostInvalidPasswordNewLock() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "test_pwd";
        String actualPassword = "test_pwd_1";
        User testUser = new User(username, actualPassword);
        String expectedResp = new Gson().toJson(new SigninServlet.Response(
                false,
                null,
                "Invalid password."
                ));
        // Make sure no lock for this user yet
        attemptLocks.clear();

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        Mockito.doReturn(testUser).when(spyAPI).getUser(anyString());
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
        verify(attemptLocks).put(eq(username), any(UserLock.class));
    }

    @Test
    public void testDoPostInvalidPasswordNewRecord() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "test_pwd";
        String actualPassword = "test_pwd_1";
        User testUser = new User(username, actualPassword);
        String expectedResp = new Gson().toJson(new SigninServlet.Response(
                false,
                null,
                "Invalid password."
                ));
        // Make sure there is a lock for this user
        attemptLocks.clear();
        attemptLocks.put(username, spyLock);

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        Mockito.doReturn(testUser).when(spyAPI).getUser(anyString());
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
        verify(spyLock).record();
    }

    @Test
    public void testDoPostInvalidPasswordWithDB() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "test_pwd";
        String actualPassword = "test_pwd_1";
        new User(username, actualPassword);
        String expectedResp = new Gson().toJson(new SigninServlet.Response(
                false,
                null,
                "Invalid password."));

        // Pre-test: Make sure user is not present, otherwise change username
        while (spyAPI.getUser(username) != null) {
            username += "_1";
        }
        // Add user
        spyAPI.addUser(username, actualPassword);

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostSuccessfulLogin() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "test_pwd";
        int maxTimeOut = 120;
        User testUser = new User(username, password);
        testUser.setPasswordHash();     // Need to hash the password
        String expectedResp = new Gson().toJson(new SigninServlet.Response(true, null));
        // Clean attemptLocks
        attemptLocks.clear();

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        Mockito.doReturn(testUser).when(spyAPI).getUser(anyString());
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockSession).setAttribute("user", username);
        verify(mockSession).setAttribute("isLoggedIn", true);
        verify(mockSession).setMaxInactiveInterval(maxTimeOut);
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostSuccessfulLoginClearLock() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "test_pwd";
        int maxTimeOut = 120;
        User testUser = new User(username, password);
        testUser.setPasswordHash();     // Need to hash the password
        String expectedResp = new Gson().toJson(new SigninServlet.Response(true, null));
        // Setup lock
        attemptLocks.put(username, spyLock);

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        Mockito.doReturn(testUser).when(spyAPI).getUser(anyString());
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockSession).setAttribute("user", username);
        verify(mockSession).setAttribute("isLoggedIn", true);
        verify(mockSession).setMaxInactiveInterval(maxTimeOut);
        verify(mockPw).print(expectedResp);
        verify(spyLock).clear();
    }

    @Test
    public void testDoPostSuccessfulLoginWithDB() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "test_pwd";
        int maxTimeOut = 120;
        new User(username, password);
        String expectedResp = new Gson().toJson(new SigninServlet.Response(true, null));

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Pre-test: Make sure user is not present, otherwise change username
        while (spyAPI.getUser(username) != null) {
            username += "_1";
        }
        // Add user
        spyAPI.addUser(username, password);

        // Call method
        servlet.doPost(mockRequest, mockResponse);

        // Verify
        verify(mockSession).setAttribute("user", username);
        verify(mockSession).setAttribute("isLoggedIn", true);
        verify(mockSession).setMaxInactiveInterval(maxTimeOut);
        verify(mockPw).print(expectedResp);
    }

    @Test
    public void testDoPostIntegrationUserLocked() throws IOException, SQLException {
        // Test data
        String username = "test_name";
        String password = "test_pwd";
        String actualPassword = "test_pwd_1";
        User testUser = new User(username, actualPassword);
        String expectedResp1 = new Gson().toJson(new SigninServlet.Response(
                false,
                null,
                "Invalid password."));
        String expectedResp2 = new Gson().toJson(new SigninServlet.Response(
                false,
                "You have made 3 failed login attempts in the past minute. " +
                        "Please try again a minute later.",
                null));

        // Clear Attempt lock
        attemptLocks.clear();

        // Mocking behavior
        when(mockRequest.getParameter("username")).thenReturn(username);
        when(mockRequest.getParameter("password")).thenReturn(password);
        // make sure when creating new lock, the new lock is our spy lock
        // Need some work on the Mockito matchers to tell the second argument passed to put() is not spyLock itself
        // Otherwise since we are calling put() method in Answer(), this would result in infinite call stack resulting
        // in stack overflow.
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // Hijack and insert our own spy lock
                // Make sure when act as we are initializing, we also put one record
                spyLock.record();
                attemptLocks.put(username, spyLock);
                return null;
            }
        }).when(attemptLocks).put(
                eq(username),
                and(any(UserLock.class), not(eq(spyLock))));
        // Mock 10 seconds time span between each call to spyLock.record().
        Mockito.doAnswer(new Answer() {
            private Instant timestamp = fromDateString("2020-10-01");

            @Override
            public Object answer(InvocationOnMock invocation) {
                // Add 10 seconds to timestamp
                timestamp = timestamp.plus(10, ChronoUnit.SECONDS);
                return timestamp;
            }
        }).when(spyLock).getCurrentTime();

        Mockito.doReturn(testUser).when(spyAPI).getUser(anyString());
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Call method 3 times
        servlet.doPost(mockRequest, mockResponse);
        servlet.doPost(mockRequest, mockResponse);
        servlet.doPost(mockRequest, mockResponse);

        // Verify the lock is indeed locked and invalid message for 3 times
        assertTrue((boolean) Whitebox.getInternalState(spyLock, "locked"));
        verify(mockPw, times(3)).print(expectedResp1);

        // Call method once again
        servlet.doPost(mockRequest, mockResponse);

        // Verify the lock is still locked and we are told once we have tried too much
        assertTrue((boolean) Whitebox.getInternalState(spyLock, "locked"));
        verify(mockPw, times(1)).print(expectedResp2);

        // Now change the time span to mock another request more than a minite later
        Mockito.doAnswer(new Answer() {
            private Instant timestamp = fromDateString("2020-10-01");

            @Override
            public Object answer(InvocationOnMock invocation) {
                // Add 10 seconds to timestamp
                timestamp = timestamp.plus(120, ChronoUnit.SECONDS);
                return timestamp;
            }
        }).when(spyLock).getCurrentTime();

        // Call method once again
        servlet.doPost(mockRequest, mockResponse);

        // Verify that lock is no longer locked and again we are told invalid password
        assertFalse((boolean) Whitebox.getInternalState(spyLock, "locked"));
        verify(mockPw, times(4)).print(expectedResp1);
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
