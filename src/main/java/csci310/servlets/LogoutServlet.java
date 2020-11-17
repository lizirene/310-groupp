package csci310.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/portfolio/logout")
public class LogoutServlet extends HttpServlet {
    static final long serialVersionUID = 1;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Invalidate the session
        req.getSession().invalidate();
        // Redirect to login page
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }
}
