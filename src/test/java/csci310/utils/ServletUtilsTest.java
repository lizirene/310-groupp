package csci310.utils;

import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@RunWith(MockitoJUnitRunner.class)
public class ServletUtilsTest extends Mockito {

    // get constructor coverage
    ServletUtils servletUtils = new ServletUtils();

    static class TestRespObj {
        public String testStr;
        public int testInt;

        TestRespObj(String testStr, int testInt) {
            this.testStr = testStr;
            this.testInt = testInt;
        }
    }

    @Test
    public void testSendResponse() throws IOException {
        // Test data
        TestRespObj testRespObj = new TestRespObj("abc", 123);

        // Mock object
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        PrintWriter mockPw = mock(PrintWriter.class);

        // Mocking behavior
        when(mockResponse.getWriter()).thenReturn(mockPw);

        // Method call
        ServletUtils.sendResponse(mockResponse, testRespObj);

        // Verify and assert
        verify(mockResponse).setContentType("application/json");
        verify(mockResponse).setCharacterEncoding("UTF-8");
        verify(mockResponse).setStatus(200);
        verify(mockPw).print((new Gson().toJson(testRespObj)));
    }
}