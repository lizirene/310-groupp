package csci310.servlets;

import csci310.Database.DatabaseAPI;
import csci310.utils.ServletUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * API Interface: remove stock from portfolio
 *
 *  URL Endpoint: /portfolio/remove-stock
 *
 *  Request:
 *      {
 *          stockId : int
 *      }
 *
 *  Response:
 *     {
 *       status : bool,
 *       message : string
 *     }
 */

@WebServlet("/portfolio/remove-stock")
public class PortfolioRemoveStock extends HttpServlet {
    static final long serialVersionUID = 1;

    private DatabaseAPI dbApi = DatabaseAPI.getInstance();

    // Overload constructors for dependency injection
    public PortfolioRemoveStock() throws SQLException, ClassNotFoundException {
        super();
    }

    public PortfolioRemoveStock(DatabaseAPI dbApi) throws SQLException, ClassNotFoundException {
        super();
        this.dbApi = dbApi;
    }

    // Response object
    static class Response {
        public boolean status;
        public String message;

        public Response(boolean status, String message) {
            this.status = status;
            this.message = message;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Delete stock from database
        try {
            if (req.getSession().getAttribute("user") == null) {
                ServletUtils.sendResponse(resp, new Response(false,
                        "Cannot identify user from HTTPSession. Please check if user has logged in and a " +
                                "valid session is present"));
                return;
            }
            String userName = (String) req.getSession().getAttribute("user");

            int stockId = Integer.parseInt(req.getParameter("stockId"));

            dbApi.deleteStock(userName, stockId);
            ServletUtils.sendResponse(resp, new Response(true, ""));
        } catch (NumberFormatException e) {
            ServletUtils.sendResponse(resp, new Response(false,
                    "Cannot identify 'stockId' field in the POST form."));
        } catch (SQLException e) {
            ServletUtils.sendResponse(resp, new Response(false,
                    "SQL Exception"));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
