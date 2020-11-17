package csci310.Database;

import csci310.model.Stock;
import csci310.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import static org.junit.Assert.*;

public class DatabaseAPITest {
    private DatabaseAPI databaseAPI;
    @Before
    public void setupDatabaseAPI() throws SQLException, ClassNotFoundException {
        databaseAPI = DatabaseAPI.getInstance();
    }

    @After
    public void clearDBDriver() throws SQLException {
        databaseAPI.clearDB();
    }

    @Test
    public void testClearDB() throws SQLException {
        assertTrue(databaseAPI.clearDB());
    }

    @Test
    public void testGetStockList() throws SQLException {
        List<Stock> result;
        String username = "test_name";
        String passwordHash = "test_hash";
        Stock stock = new Stock(0, "aapl", 10, username,
                Date.from(LocalDate.parse("2020-10-01").atStartOfDay(ZoneId.systemDefault()).toInstant()),
                Date.from(LocalDate.parse("2020-10-01").atStartOfDay(ZoneId.systemDefault()).toInstant()));

        // user not exist
        result = databaseAPI.getStockList(username);
        assertNull(result);

        // user exist but no stock
        databaseAPI.addUser(username, passwordHash);
        result = databaseAPI.getStockList(username);
        assertEquals(0, result.size());

        // user has some stock
        databaseAPI.addStock(username, stock);
        result = databaseAPI.getStockList(username);
        assertEquals(stock.getTicker(), result.get(0).getTicker());
    }

    @Test
    public void testAddStock() throws SQLException {
        String username = "test_name";
        String passwordHash = "test_hash";
        Stock stock = new Stock("aapl", 10, username,
                Date.from(LocalDate.parse("2020-10-01").atStartOfDay(ZoneId.systemDefault()).toInstant()),
                Date.from(LocalDate.parse("2020-10-01").atStartOfDay(ZoneId.systemDefault()).toInstant()));
        int result;

        // user does not exist
        result = databaseAPI.addStock(username, stock);
        assertEquals(-1, result);

        // user exist
        databaseAPI.addUser(username, passwordHash);
        assertEquals(1, databaseAPI.addStock(username, stock));
        assertEquals(stock.getTicker(),
                     databaseAPI.getStockList(username)
                     .get(0).getTicker());
    }

    @Test
    public void testDeleteStock() throws SQLException {
        String username = "test_name";
        String passwordHash = "test_hash";
        String ticker = "aapl";
        Stock stock = new Stock(ticker, 10, username,
                Date.from(LocalDate.parse("2020-10-01").atStartOfDay(ZoneId.systemDefault()).toInstant()),
                Date.from(LocalDate.parse("2020-10-01").atStartOfDay(ZoneId.systemDefault()).toInstant()));
        boolean result;

        // user does not exist
        result = databaseAPI.deleteStock(username, 1);
        assertFalse(result);

        // user does exist, stock does not exist
        result = databaseAPI.addUser(username, passwordHash);
        assertTrue(result);

        // user does exist, stock does exist
        databaseAPI.addStock(username, stock);
        databaseAPI.deleteStock(username, 1);
        assertEquals(0, databaseAPI.getStockList(username).size());
    }

    @Test
    public void testAddUser() throws SQLException {
        String username = "test_name";
        String password = "test_pwd";
        boolean result = databaseAPI.addUser(username, password);
        assertTrue(result);

        User userGotten = databaseAPI.getUser(username);
        assertEquals(username, userGotten.getUsername());
        assertEquals(DigestUtils.sha256Hex(password), userGotten.getPasswordHash());
    }

    @Test
    public void testGetUser() throws SQLException {
        String username = "test_name";
        String passwordHash = "test_hash";

        // user does not exist
        User result = databaseAPI.getUser(username);
        assertNull(result);

        // user does exist
        databaseAPI.addUser(username, passwordHash);
        result = databaseAPI.getUser(username);
        assertEquals(username, result.getUsername());
    }

    @Test
    public void testDeleteUser() throws SQLException {
        assertFalse(databaseAPI.deleteUser("user123"));
        databaseAPI.addUser("user123", "123456");
        assertTrue(databaseAPI.deleteUser("user123"));
    }
}
