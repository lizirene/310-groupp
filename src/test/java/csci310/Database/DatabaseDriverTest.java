package csci310.Database;

import csci310.model.Stock;
import csci310.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.SQLException;

import static org.junit.Assert.*;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class DatabaseDriverTest {

	private DatabaseDriver databaseDriver;

	@Before
	public void setupDbDriver() throws SQLException, ClassNotFoundException {
		databaseDriver = new DatabaseDriver();
		databaseDriver.init();
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void clearDbDriver() throws SQLException {
		databaseDriver.destruct();
	}

	@Test
	public void testInit() throws SQLException, ClassNotFoundException {
		// test normal init
		databaseDriver.destruct();
		assertTrue(databaseDriver.init());

		// test double init
		assertTrue(databaseDriver.init());
	}

	@Test
	public void testDestruct() throws SQLException {
		assertTrue(databaseDriver.destruct());
	}

	@Test
	public void testDestructNULL() throws SQLException {
		DatabaseDriver databaseDriverNULL = new DatabaseDriver();
		Whitebox.setInternalState(databaseDriverNULL, "connection", null);
		assertTrue(databaseDriverNULL.destruct());
	}

	@Test
	public void testInsertUser() throws SQLException {
		// insert a user to the db
		User user = new User("username", "password");
		assertTrue(databaseDriver.insertUser(user.getUsername(), user.getPasswordHash()));

		// get the user from db
		User userGotten = databaseDriver.getUser(user.getUsername());
		assertEquals(user.getUsername(), userGotten.getUsername());

		// insert the same user again, nothing change in db
		assertFalse(databaseDriver.insertUser(user.getUsername(), user.getPasswordHash()));
		assertEquals(user.getUsername(), userGotten.getUsername());
	}

	@Test
	public void testInsertUserStock() throws SQLException {
		User user = new User("username", "password");
		Stock stock = new Stock("AAPL", 10, user.getUsername(), "2020-10-01");

		// stock doesn't exist, add row
		databaseDriver.insertUser(user.getUsername(), user.getPasswordHash());
		int stockIdGotten = databaseDriver.insertUserStock(user, stock);
		assertEquals(1, stockIdGotten);
		User userGotten = databaseDriver.getUser(user.getUsername());
		assertEquals(stock.getTicker(),
					 userGotten.getStocks()
					 .get(stockIdGotten).getTicker());

		// stock does exist, insert another row
		databaseDriver.insertUserStock(user, stock);
		userGotten = databaseDriver.getUser(user.getUsername());
		assertEquals(2, userGotten.getStock(2).getStockId());

		// mock a failure test
	}

	@Test
	public void testGetUser() throws SQLException {
		User user = new User("username", "password");
		User userGotten;
		// test null case
		userGotten = databaseDriver.getUser(user.getUsername());
		assertNull(userGotten);

		// test normal
		databaseDriver.insertUser(user.getUsername(), user.getPasswordHash());
		Stock stock = new Stock(0, "AAPL", 10, user.getUsername(), "2020-10-01");
		databaseDriver.insertUserStock(user, stock);
		userGotten = databaseDriver.getUser(user.getUsername());
		assertEquals(user.getUsername(), userGotten.getUsername());
	}

	@Test
	public void testDeleteUser() throws SQLException {
		databaseDriver.insertUser("username123", "password123");
		assertTrue(databaseDriver.deleteUser("username123"));
		assertFalse(databaseDriver.deleteUser("username123"));
	}

	@Test
	public void testDeleteUserStock() throws SQLException {
		User user = new User("username", "password");
		Stock stock = new Stock("AAPL", 10, user.getUsername(),"2020-10-01");
		databaseDriver.insertUser(user.getUsername(), user.getPasswordHash());

		// stock not exist
		assertTrue(databaseDriver.deleteUserStock(user, 1));

		// stock exist
		databaseDriver.insertUserStock(user, stock);
		databaseDriver.deleteUserStock(user, 1);
		User userGotten = databaseDriver.getUser(user.getUsername());
		assertNull(userGotten.getStocks().get(stock.getTicker()));
	}
}
