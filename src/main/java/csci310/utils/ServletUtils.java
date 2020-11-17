package csci310.utils;

import com.google.gson.Gson;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServletUtils {

	/**
	 * send servlet response
	 * @param {HttpServletResponse} resp
	 * @param {Object} respObj
	 * @throws IOException
	 */
    public static void sendResponse(HttpServletResponse resp, Object respObj) throws IOException {
        // Set response context
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        // Turn response object into Json object then into Json string
        String respString = new Gson().toJson(respObj);
        // Send and set response status
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().print(respString);
        resp.getWriter().flush();
    }
}
