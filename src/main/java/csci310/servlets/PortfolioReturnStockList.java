package csci310.servlets;

import csci310.Database.DatabaseAPI;
import csci310.model.Stock;
import csci310.utils.ServletUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * API Interface: return a list of stocks in the current user's portfolio
 *
 *  URL Endpoint: /portfolio/stock-list
 *
 *  Response:
 *     {
 *       status : bool,
 *       message : string,
 *       data : [
 *         {
 *           ownerUsername : string,
 *           ticker : string,
 *           quantity : int,
 *           dateBought : Unix Timestamp,
 *           (optional field) dateSold : Unix Timestamp
 *         }
 *       ]
 *     }
 */

@WebServlet("/portfolio/stock-list")
public class PortfolioReturnStockList extends HttpServlet {
    static final long serialVersionUID = 1;

    private DatabaseAPI dbApi = DatabaseAPI.getInstance();

    // Overload constructors for dependency injection
    public PortfolioReturnStockList() throws SQLException, ClassNotFoundException {
        super();
    }

    public PortfolioReturnStockList(DatabaseAPI dbApi) throws SQLException, ClassNotFoundException {
        super();
        this.dbApi = dbApi;
    }

    // Response object
    static class Response {
        public boolean status;
        public String message;
        List<Stock> data;

        public Response(boolean status, String message, List<Stock> data) {
            this.status = status;
            this.message = message;
            this.data = data;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Fetch data from DB
        try {
            if (req.getSession().getAttribute("user") == null) {
                ServletUtils.sendResponse(resp, new Response(
                        false,
                        "Cannot identify user from HTTPSession. Please check if user has logged in and a " +
                                "valid session is present",
                        null));
                return;
            }
            String username = (String) req.getSession().getAttribute("user");
            List<Stock> stockList = dbApi.getStockList(username);
            // Send response
            ServletUtils.sendResponse(resp, new Response(true, "", stockList));
        } catch (SQLException e) {
            ServletUtils.sendResponse(resp, new Response(false, "SQL Exception", null));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
