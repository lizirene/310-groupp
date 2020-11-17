package csci310.servlets;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class LogoutServletTest extends Mockito {

    @InjectMocks
    LogoutServlet servlet;

    @Mock
    HttpServletRequest mockRequest;

    @Mock
    HttpServletResponse mockResponse;

    @Mock
    HttpSession mockSession;

    @Test
    public void testDoGet() throws IOException {
        // Mocking behavior
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockRequest.getContextPath()).thenReturn("");

        // Call method
        servlet.doGet(mockRequest, mockResponse);

        // Verify
        verify(mockSession).invalidate();
        verify(mockResponse).sendRedirect("/login.jsp");
    }
}