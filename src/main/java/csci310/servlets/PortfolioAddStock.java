package csci310.servlets;

import csci310.Database.DatabaseAPI;
import csci310.model.Stock;
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
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/portfolio/add-stock")
public class PortfolioAddStock extends HttpServlet {
    static final long serialVersionUID = 1;

    private DatabaseAPI dbApi = DatabaseAPI.getInstance();
    private final StockTrading st = new StockTrading();

    private final static DateTimeFormatter dateFieldFormatter = DateTimeFormatter.ofPattern("M/d/uuuu");


    // Overload constructors for dependency injection
    public PortfolioAddStock() throws SQLException, ClassNotFoundException {
        super();
    }

    public PortfolioAddStock(DatabaseAPI dbApi) throws SQLException, ClassNotFoundException {
        super();
        this.dbApi = dbApi;
    }

    // Response object
    static class Message {
        String ticker;
        String quantity;
        String dateBought;
        String dateSold;

        Message() {}
        Message(String ticker, String quantity, String dateBought, String dateSold) {
            this.ticker = ticker;
            this.quantity = quantity;
            this.dateBought = dateBought;
            this.dateSold = dateSold;
        }
    }

    static class Response {
        int stockId;
        boolean status;
        Message message;

        Response() {
            this.stockId = -1;
            this.status = true;
            this.message = new Message();
        }
        Response(int stockId, boolean status, Message message) {
            this.stockId = stockId;
            this.status = status;
            this.message = message;
        }
        Response(int stockId, boolean status, String ticker, String quantity, String dateBought, String dateSold) {
            this.stockId = stockId;
            this.status = status;
            this.message = new Message(ticker, quantity, dateBought, dateSold);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if (req.getSession().getAttribute("user") == null) {
                ServletUtils.sendResponse(resp, new Response(
                        -1,
                        false,
                        "Your session has expired. Please log in again.",
                        null,
                        null,
                        null));
                return;
            }
            String userName = (String) req.getSession().getAttribute("user");

            Response respObj = new Response();
            // Verify ticker value
            String ticker = req.getParameter("ticker").trim();
            Pattern pattern = Pattern.compile("^[a-zA-Z]{1,5}$");
            Matcher matcher = pattern.matcher(ticker);
            if (ticker.equals("")) {
                respObj.status = false;
                respObj.message.ticker = "Ticker field should not be empty";
            }
            else if (!matcher.find()) {
                respObj.status = false;
                respObj.message.ticker = "Ticker should consist of letters only and should not be " +
                        "more than 5 characters long";
            }
            else {
                // Use API to verify that the ticker is a NASDAQ and/or NYSE stock
                String exchangeCode = st.getStockExchangeCode(ticker);
                if (!exchangeCode.contains("NASDAQ") && !exchangeCode.contains("NYSE")) {
                    respObj.status = false;
                    respObj.message.ticker = "Please enter a NASDAQ or NYSE stock ticker";
                }
            }

            // Validate quantity field
            int quantity = 0;
            if (req.getParameter("quantity").equals("")) {
                respObj.status = false;
                respObj.message.quantity = "Number of shares field should not be empty";
            }
            else {
                quantity = Integer.parseInt(req.getParameter("quantity"));
                if (quantity <= 0) {
                    respObj.status = false;
                    respObj.message.quantity = "Number of shares should be a positive integer";
                }
            }

            // Validate dateBought field
            String dateBoughtString = req.getParameter("dateBought").trim();
            LocalDate dateBoughtLocalDate = null;
            if (dateBoughtString.equals("") || dateBoughtString.equals("null")) {
                respObj.status = false;
                respObj.message.dateBought = "Date bought field should not be empty";
            }
            else {
                try {
                    dateBoughtLocalDate = LocalDate.parse(dateBoughtString, dateFieldFormatter.withResolverStyle(ResolverStyle.STRICT));
                }
                catch (DateTimeParseException e) {
                    respObj.status = false;
                    respObj.message.dateBought = "Please enter a valid date of format MM/DD/YYYY";
                }
            }

            // Validate dateSold field
            String dateSoldString = req.getParameter("dateSold").trim();
            LocalDate dateSoldLocalDate = null;
            if (!dateSoldString.equals("") && !dateSoldString.equals("null")) {
                try {
                    dateSoldLocalDate = LocalDate.parse(dateSoldString, dateFieldFormatter.withResolverStyle(ResolverStyle.STRICT));
                } catch (DateTimeParseException e) {
                    respObj.status = false;
                    respObj.message.dateSold = "Please enter a valid date of format MM/DD/YYYY";
                }
            }

            // Check that dateBought is no later than dateSold
            if (dateSoldLocalDate != null && dateBoughtLocalDate != null && dateBoughtLocalDate.compareTo(dateSoldLocalDate) > 0) {
                respObj.status = false;
                respObj.message.dateBought = "Date bought cannot be later than Date sold";
                respObj.message.dateSold = "Date sold cannot be earlier than Date bought";
            }

            // Send response
            if (respObj.status){
                // Decide whether to insert into Database depending on stockType value
                String stockType = req.getParameter("stockType");
                if (stockType.equals("portfolio")) {
                    Date dateBought = Date.from(dateBoughtLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    Date dateSold = dateSoldLocalDate == null? null : Date.from(dateSoldLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                    Stock newStock = new Stock(ticker, quantity, userName, dateBought, dateSold);
                    respObj.stockId = dbApi.addStock(userName, newStock);
                }
            }
            ServletUtils.sendResponse(resp, respObj);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
