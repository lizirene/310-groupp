package csci310.servlets;

import csci310.Database.DatabaseAPI;
import csci310.model.Stock;
import csci310.trading.StockEndpoint;
import csci310.trading.StockTrading;
import csci310.utils.ServletUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/portfolio/value-and-increase")
public class PortfolioReturnValueAndIncrease extends HttpServlet {
    static final long serialVersionUID = 1;

    private DatabaseAPI dbApi = DatabaseAPI.getInstance();

    // Overload constructors for dependency injection
    public PortfolioReturnValueAndIncrease() throws SQLException, ClassNotFoundException {
        super();
    }

    public PortfolioReturnValueAndIncrease(DatabaseAPI dbApi) throws SQLException, ClassNotFoundException {
        super();
        this.dbApi = dbApi;
    }

    // Response data structures
    public static class Response {
        public boolean status;
        public String message;
        public String username;
        public double totalValue;
        public double increase;

        Response(boolean status, String message, String username, double totalValue, double increase) {
            this.status = status;
            this.message = message;
            this.username = username;
            this.totalValue = totalValue;
            // Cap increase value to be between -10 and 10
//            double capAbove = increase > 10 ? 10 : increase;
//            this.increase = capAbove < -10? -10 : capAbove;
            this.increase = increase;
        }
    }

    // Util method to be mocked during testing
    LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Get user name
            if (req.getSession().getAttribute("user") == null) {
                ServletUtils.sendResponse(resp, new Response(
                        false,
                        "Cannot identify user from HTTPSession. Please check if user has logged in and a " +
                                "valid session is present",
                        null,
                        0,
                        0));
                return;
            }
            String userName = (String) req.getSession().getAttribute("user");

            // Initialize empty response
            Response respObj = null;
            // Fetch stock list of this user from DB
            List<Stock> stockList = dbApi.getStockList(userName);
            // If user has no stock, send empty package back and return directly
            if (stockList.size() == 0) {
                respObj = new Response(true, "", userName, 0, 0);
                ServletUtils.sendResponse(resp, respObj);
                return;
            }

            // Use stock trading API to obtain Portfolio value
            // Get today and yesterday date in correct format
            LocalDate today = getCurrentDate();
            LocalDate periodStart = today.minusDays(14);
            // We will get daily stock data from up to 14 days before to yesterday
            Map<String, List<StockEndpoint>> portfolioStockData = new HashMap<>();
            StockTrading st = new StockTrading();
            for (Stock entry : stockList) {
                List<StockEndpoint> stockData = st.getHistoricalPrices(entry.getTicker(), periodStart.toString(), today.toString(), StockTrading.ResampleFreq.DAILY);
                portfolioStockData.put(entry.getTicker(), stockData);
            }


            // Check if last entry of stockData has date of yesterday
            // Get collected data of any one stock
            List<StockEndpoint> exampleData = portfolioStockData.entrySet().iterator().next().getValue();
            String lastEntryDateStr = exampleData.get(exampleData.size() - 1).date.substring(0, 10);
            String secondToLastEntryDateStr = exampleData.get(exampleData.size() - 2).date.substring(0, 10);
            LocalDate lastEntryDate = LocalDate.parse(lastEntryDateStr);
            LocalDate secondToLastEntryDate = LocalDate.parse(secondToLastEntryDateStr);

            // Calculate the total portfolio value of the last entry and second to last entry
            // Only add to portfolio value if the date of the value is no earlier than dateBought and
            // no later than dateSold
            double lastVal = 0;
            double secondToLastVal = 0;
            for (Stock entry : stockList) {
                List<StockEndpoint> data = portfolioStockData.get(entry.getTicker());
                double lastPrice = data.get(data.size() - 1).close;
                double secondToLastPrice = data.get(data.size() - 2).close;

//                LocalDate dateBought = LocalDate.parse(entry.getDateBought());
//                LocalDate dateSold = entry.getDateSold() == null ? null : LocalDate.parse(entry.getDateSold());
                LocalDate dateBought = entry.getDateBought().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate dateSold = entry.getDateSold() == null ? null : entry.getDateSold().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                if (dateBought.compareTo(lastEntryDate) <= 0 && (dateSold == null || dateSold.compareTo(lastEntryDate) > 0)) {
                    lastVal += entry.getQuantity() * lastPrice;
                }
                if (dateBought.compareTo(secondToLastEntryDate) <= 0 && (dateSold == null || dateSold.compareTo(secondToLastEntryDate) > 0)) {
                    secondToLastVal += entry.getQuantity() * secondToLastPrice;
                }
            }

            double increase;
            if (lastVal == 0.0 && secondToLastVal == 0.0) {
                increase = 0.0;
            } else if (secondToLastVal == 0) {
                // If yesterday's value is 0, and today;s value is positive,
                // Then treat yesterday's value as 1.
                increase = lastVal;
            } else {
                increase = (lastVal - secondToLastVal) / secondToLastVal;
            }
            respObj = new Response(true, "", userName, lastVal, increase);

            // Send response
            ServletUtils.sendResponse(resp, respObj);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
