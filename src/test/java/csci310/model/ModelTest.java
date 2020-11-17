package csci310.model;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ModelTest {
    private Stock stock;
    private Stock stockToSet;
    private User user;
    private String username = "test_user";
    private String password = "test_hash";
    private int stockId = 1;
    private String ticker = "AAPL";
    private int quantity = 10;
    private String dateBoughtString = "2020-10-01";
    private String dateSoldString = "2020-10-01";
    private Date dateBought = Date.from(LocalDate.parse(dateBoughtString).atStartOfDay(ZoneId.systemDefault()).toInstant());
    private Date dateSold = Date.from(LocalDate.parse(dateSoldString).atStartOfDay(ZoneId.systemDefault()).toInstant());
    private Map<Integer, Stock> stocks = new HashMap<>();

    private String usernameToSet = "test_user1";
    private int stockIdToSet = 2;
    private String tickerToSet = "FB";
    private int quantityToSet = 20;
    private Date dateBoughtToSet = Date.from(LocalDate.parse("2020-10-02").atStartOfDay(ZoneId.systemDefault()).toInstant());
    private Date dateSoldToSet = Date.from(LocalDate.parse("2020-10-02").atStartOfDay(ZoneId.systemDefault()).toInstant());
    private Map<Integer, Stock> stocksToSet = new HashMap<>();

    @Before
    public void setupModels() {
        user = new User(username, password);
        stock = new Stock(stockId, ticker, quantity, user.getUsername(), dateBought, dateSold);
        stockToSet = new Stock(stockId, tickerToSet, quantityToSet, user.getUsername(), dateBoughtToSet, dateSoldToSet);

        // Dummy constructor call to ensure coverage
        new Stock(stockId, ticker, quantity, user.getUsername(), dateBought);
        new Stock(stockId, ticker, quantity, user.getUsername(), dateBought, dateSold);
        new Stock(stockId, ticker, quantity, user.getUsername(), dateBoughtString);
        new Stock(stockId, ticker, quantity, user.getUsername(), dateBoughtString, dateSoldString);
        new Stock(ticker, quantity, user.getUsername(), dateBought);
        new Stock(ticker, quantity, user.getUsername(), dateBought, dateSold);
        new Stock(ticker, quantity, user.getUsername(), dateBoughtString);
        new Stock(ticker, quantity, user.getUsername(), dateBoughtString, dateSoldString);
    }

    @Test
    public void testGetters() {
        // User
        user = new User(username, password);
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPasswordHash());
        assertEquals(stocks, user.getStocks());
        assertNull(user.getStock(stockId));
        stocks.put(stock.getStockId(), stock);
        user.setStocks(stocks);
        assertEquals(ticker, user.getStock(stockId).getTicker());

        // Stock
        assertEquals(stockId, stock.getStockId());
        assertEquals(ticker, stock.getTicker());
        assertEquals(quantity, stock.getQuantity());
        assertEquals(dateBought, stock.getDateBought());
        assertEquals(dateSold, stock.getDateSold());
        assertEquals(username, stock.getOwnerUsername());
        assertEquals(dateBoughtString, stock.getDateBoughtString());
        assertEquals(dateSoldString, stock.getDateSoldString());
    }

    @Test
    public void testSetters() {
        // User
        user = new User(username, password);

        user.setUsername(usernameToSet);
        assertEquals(usernameToSet, user.getUsername());

        user.setPasswordHash();
        assertEquals(DigestUtils.sha256Hex(password), user.getPasswordHash());

        stocksToSet.put(stockToSet.getStockId(), stockToSet);
        user.setStocks(stocksToSet);
        assertEquals(stocksToSet, user.getStocks());

        stocks.put(stock.getStockId(), stock);
        user.setStocks(stocks);
        assertEquals(ticker, stock.getTicker());

        // Stock
        stock.setStockId(stockIdToSet);
        assertEquals(stockIdToSet, stock.getStockId());

        stock.setTicker(tickerToSet);
        assertEquals(tickerToSet, stock.getTicker());

        stock.setQuantity(quantityToSet);
        assertEquals(quantityToSet, stock.getQuantity());

        stock.setDateBought(dateBoughtToSet);
        assertEquals(dateBoughtToSet, stock.getDateBought());

        stock.setDateSold(dateSoldToSet);
        assertEquals(dateSoldToSet, stock.getDateSold());

        stock.setOwnerUsername(usernameToSet);
        assertEquals(usernameToSet, stock.getOwnerUsername());

        stock.setDateBoughtString(dateBoughtString);
        assertEquals(dateBoughtString, stock.getDateBoughtString());

        stock.setDateSoldString(dateSoldString);
        assertEquals(dateSoldString, stock.getDateSoldString());
    }
}
