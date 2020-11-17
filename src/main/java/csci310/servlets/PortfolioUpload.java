package csci310.servlets;

import csci310.Database.DatabaseAPI;
import csci310.model.Stock;
import csci310.trading.StockTrading;
import csci310.utils.ServletUtils;
import org.apache.commons.io.FilenameUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@WebServlet("/portfolio/upload")
@MultipartConfig
public class PortfolioUpload extends HttpServlet {
    static final long serialVersionUID = 1;

    private DatabaseAPI dbApi = DatabaseAPI.getInstance();
    private StockTrading st = new StockTrading();
    private final static DateTimeFormatter dateFieldFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public PortfolioUpload() throws SQLException, ClassNotFoundException {
    }

    public PortfolioUpload(DatabaseAPI dbApi, StockTrading st) throws SQLException, ClassNotFoundException {
        this.dbApi = dbApi;
        this.st = st;
    }

    // POST response
    static class Response {
        boolean status;
        String message;

        Response(boolean status, String message) {
            this.status = status;
            this.message = message;
        }
    }

    // CSV Data object
    static class StockMetadata {
        String ticker;
        String quantity;
        String dateBought;
        String dateSold;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            if (req.getSession().getAttribute("user") == null) {
                ServletUtils.sendResponse(resp, new Response(
                        false,
                        "Your session has expired. Please log in again."));
                return;
            }
            String userName = (String) req.getSession().getAttribute("user");

            // Retrieve multipart form data
            Part filePart = req.getPart("file");
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            InputStream fileContentStream = filePart.getInputStream();

            // Check that uploaded file is csv
            if (!FilenameUtils.getExtension(fileName).equals("csv")) {
                ServletUtils.sendResponse(resp, new Response(
                        false,
                        "Please upload a CSV file"
                ));
                return;
            }

            // Parse the CSV file into list of stock metadata objects
            List<StockMetadata> stockMetadataList = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(fileContentStream, StandardCharsets.UTF_8));
            String line;
            int lineCount = 0;
            while ((line = br.readLine()) != null) {
                // If first line, check CSV header field name
                if (lineCount++ == 0) {
                    if (!line.equals("ticker,quantity,dateBought,dateSold")) {
                        ServletUtils.sendResponse(resp, new Response(
                                false,
                                "Please make sure the CSV's field names are correct: ticker, quantity, dateBought, dateSold"
                        ));
                        return;
                    }
                }
                else {
                    // Split by comma
                    String[] content = line.split(",");
                    if (!(content.length == 4)) {
                        ServletUtils.sendResponse(resp, new Response(
                                false,
                                "Some data in the CSV file are missing."
                        ));
                        return;
                    }

                    StockMetadata metadata = new StockMetadata();
                    metadata.ticker = content[0];
                    metadata.quantity = content[1];
                    metadata.dateBought = content[2];
                    metadata.dateSold = content[3];
                    stockMetadataList.add(metadata);
                }
            }
            System.out.println(stockMetadataList);

            // Check metadata line by line
            List<Stock> stockToAdd = new ArrayList<>();
            for (StockMetadata metadata : stockMetadataList) {
                // Check ticker, using api
                String exchangeCode = st.getStockExchangeCode(metadata.ticker);
                if (!exchangeCode.contains("NASDAQ") && !exchangeCode.contains("NYSE")) {
                    ServletUtils.sendResponse(resp, new Response(
                            false,
                            "Found invalid ticker. Please check if all tickers are valid " +
                                    "NASDAQ or NYSE stock tickers."
                    ));
                    return;
                }
                // Check quantity, by trying to parse and if success check if positive
                int quantityNum = 0;
                try {
                    quantityNum = Integer.parseInt(metadata.quantity);
                    if (quantityNum <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    ServletUtils.sendResponse(resp, new Response(
                            false,
                            "Found non-positive quantity or malformed quantity value. " +
                                    "Please check that all quantity fields are positive integers."
                    ));
                    return;
                }
                // Check dateBought and dateSold format, by trying to parse
                LocalDate dateBoughtLocalDate, dateSoldLocalDate;
                try {
                    dateBoughtLocalDate = LocalDate.parse(metadata.dateBought, dateFieldFormatter);
                    dateSoldLocalDate = LocalDate.parse(metadata.dateSold, dateFieldFormatter);
                } catch (DateTimeParseException e) {
                    ServletUtils.sendResponse(resp, new Response(
                            false,
                            "Cannot parse some of the date values. " +
                                    "Please check that all dateBought and dateSold fields are valid date " +
                                    "of format MM/DD/YYYY."
                    ));
                    return;
                }
                // Check dateBought is earlier than dateSold
                if (dateBoughtLocalDate.compareTo(dateSoldLocalDate) > 0) {
                    ServletUtils.sendResponse(resp, new Response(
                            false,
                            "Find dateBought later than dateSold. " +
                                    "Please check for each entry, dateBought is no later than dateSold."
                    ));
                    return;
                }
                // Everything's fine, add to list of stocks to be added to database
                stockToAdd.add(new Stock(
                        metadata.ticker,
                        quantityNum,
                        userName,
                        Date.from(dateBoughtLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        Date.from(dateSoldLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant())));
            }

            // Add all stocks to the database
            for (Stock stock : stockToAdd) {
                dbApi.addStock(userName, stock);
            }
            ServletUtils.sendResponse(resp, new Response(
                    true,
                    ""
            ));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
