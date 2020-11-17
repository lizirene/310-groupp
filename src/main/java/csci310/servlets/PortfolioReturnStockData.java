package csci310.servlets;

import com.google.gson.Gson;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

@WebServlet("/portfolio/stock-data")
public class PortfolioReturnStockData extends HttpServlet {
    static final long serialVersionUID = 1;

    private DatabaseAPI dbApi = DatabaseAPI.getInstance();
    private StockTrading st = new StockTrading();

    private final static DateTimeFormatter dateFieldFormatter = DateTimeFormatter.ofPattern("M/d/uuuu");

    // Overload constructors for dependency injection
    public PortfolioReturnStockData() throws SQLException, ClassNotFoundException {
        super();
    }

    public PortfolioReturnStockData(DatabaseAPI dbApi, StockTrading st) throws SQLException, ClassNotFoundException {
        super();
        this.dbApi = dbApi;
        this.st = st;
    }

    // Response object
    static class Message {
        String startDate;
        String endDate;

        Message() {}
        Message (String startDate, String endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    static class Response {
        boolean status;
        Message message;
        Map<String, List<StockEndpoint>> data;

        Response() {
            this.status = true;
            this.message = new Message();
            this.data = null;
        }
        Response(Map<String, List<StockEndpoint>> data) {
            this.status = true;
            this.message = null;
            this.data = data;
        }
        Response(String startDate, String endDate) {
            this.status = false;
            this.message = new Message(startDate, endDate);
            this.data = null;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if (req.getSession().getAttribute("user") == null) {
                ServletUtils.sendResponse(resp, new Response(
                        "Your session has expired. Please log in again.",
                        null));
                return;
            }
            String userName = (String) req.getSession().getAttribute("user");

            String[] tickerList = new Gson().fromJson(req.getParameter("tickerList"), String[].class);
            String startDateString = req.getParameter("startDate");
            String endDateString = req.getParameter("endDate");
            String frequency = req.getParameter("frequency");
            String stockType = req.getParameter("stockType");

            Response respObj = new Response();
            // Validate startDate if not empty
            LocalDate startDateLocalDate = null;
            LocalDate endDateLocalDate = null;
            if (!startDateString.equals("")) {
                try {
                    startDateLocalDate = LocalDate.parse(startDateString, dateFieldFormatter.withResolverStyle(ResolverStyle.STRICT));
                } catch (DateTimeParseException e) {
                    respObj.status = false;
                    respObj.message.startDate = "Please enter a valid date of format MM/DD/YYYY";
                }
            }
            // If startDate empty, use default value - the earliest dateBought of all current user's portfolio stocks
            else {
                List<Stock> stockList = dbApi.getStockList(userName);
                // If user stock is empty, then default to 3 months ago
                if (stockList.size() == 0) {
                    startDateLocalDate = LocalDate.now().minus(3, ChronoUnit.MONTHS);
                }
                // Otherwise, find earliest date bought
                else {
                    startDateLocalDate = null;
                    for (Stock s : stockList) {
                        LocalDate dateBought = s.getDateBought().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        if (startDateLocalDate == null ||
                            startDateLocalDate.compareTo(dateBought) > 0) {
                            startDateLocalDate = dateBought;
                        }
                    }
                }
            }
            // Validate endDate if not empty
            if (!endDateString.equals("")) {
                try {
                    endDateLocalDate = LocalDate.parse(endDateString, dateFieldFormatter.withResolverStyle(ResolverStyle.STRICT));
                } catch (DateTimeParseException e) {
                    respObj.status = false;
                    respObj.message.endDate = "Please enter a valid date of format MM/DD/YYYY";
                }
            }
            // if endDate empty, use default value - today's date
            else {
                endDateLocalDate = LocalDate.now();
            }

            // Check that start date is no later than end date
            if (startDateLocalDate != null && endDateLocalDate != null &&
                    startDateLocalDate.compareTo(endDateLocalDate) > 0) {
                respObj.status = false;
                respObj.message.startDate = "Start date cannot be later than End date";
                respObj.message.endDate = "End date cannot be earlier than Start date";
            }

            // If fields are invalid, send message back. Otherwise proceed
            if (!respObj.status) {
                ServletUtils.sendResponse(resp, respObj);
                return;
            }

            // Obtain data from stock trading API
            Map<String, List<StockEndpoint>> result = new HashMap<>();
            for (String ticker : tickerList) {
                List<StockEndpoint> data = st.getHistoricalPrices(
                        ticker,
                        startDateLocalDate.toString(),
                        endDateLocalDate.toString(),
                        StockTrading.ResampleFreq.valueOf(frequency.toUpperCase()));

                // Check if the date of the first entry in the fetched data coincides with request startDate.
                //  If not, fetch even earlier to obtain an entry before startData so that the returned stock data
                //  has at least request date range
                if (LocalDate.parse(data.get(0).date.substring(0, 10)).compareTo(startDateLocalDate) > 0) {
                    // Request another 7 days earlier data, and take the last entry and prepend to the data fetched above
                    List<StockEndpoint> extra = st.getHistoricalPrices(
                            ticker,
                            startDateLocalDate.minus(7, ChronoUnit.DAYS).toString(),
                            startDateLocalDate.toString(),
                            StockTrading.ResampleFreq.valueOf(frequency.toUpperCase()));
                    data.add(0, extra.get(extra.size() - 1));
                }

                result.put(ticker.toUpperCase(), data);
            }

            // If stockType is not portfolio, then return the raw data directly
            // Otherwise if portfolio, then need to aggregate into one date line
            if (!stockType.equals("portfolio")) {
                ServletUtils.sendResponse(resp, new Response(result));
            } else {
                List<Stock> stockList = dbApi.getStockList(userName);
                // Dictionary mapping ticker to list of records
                // Need to consider multiple records with same ticker value
                Map<String, List<Stock>> stockMap = new HashMap<>();
                for (Stock record : stockList) {
                    if (!stockMap.containsKey(record.getTicker())) {
                        stockMap.put(record.getTicker(), new ArrayList<>());
                    }
                    stockMap.get(record.getTicker()).add(record);
                }

                List<StockEndpoint> dataLine = new ArrayList<>();
                // Iterate over all requested stocks
                for (Map.Entry<String, List<StockEndpoint>> entry : result.entrySet()) {
                    // If data is not populated, then populate first with date and 0 value
                    if (dataLine.size() < entry.getValue().size()) {
                        for (StockEndpoint datum : entry.getValue()) {
                            dataLine.add(new StockEndpoint(datum.date, 0));
                        }
                    }
                    // Iterate over date, add to value if date is in range of [dateBought, dateSold)
                    List<Stock> recordList = stockMap.get(entry.getKey());
                    for (int i = 0; i < entry.getValue().size(); i++) {
                        StockEndpoint datum = entry.getValue().get(i);
                        StockEndpoint dataLinePoint = dataLine.get(i);
                        LocalDate date = LocalDate.parse(datum.date.substring(0, 10));

                        // Iterate over all record with corresponding ticker value and add up value
                        for (Stock record : recordList) {
                            LocalDate dateBought = record.getDateBought().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            LocalDate dateSold = record.getDateSold() == null ?
                                    LocalDate.now().plus(1, ChronoUnit.DAYS) :
                                    record.getDateSold().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            if (date.compareTo(dateBought) >= 0 && date.compareTo(dateSold) < 0) {
                                dataLinePoint.close += record.getQuantity() * datum.close;
                            }
                        }
                    }
                }

                // Turn into result map and send via response
                Map<String, List<StockEndpoint>> aggregated = new HashMap<>();
                aggregated.put("aggregated", dataLine);
                ServletUtils.sendResponse(resp, new Response(aggregated));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
