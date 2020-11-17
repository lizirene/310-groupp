package csci310.Database;

import csci310.model.Stock;
import csci310.model.User;

import java.sql.*;
import java.util.HashMap;

public class DatabaseDriver {

	// database variables
	private Connection connection = null;
	private final String jdbcURL = "jdbc:sqlite:test.db";

	// SQL statements
	private static final String INSERT_USER_SQL =
			"INSERT INTO User(username, password) " +
			"VALUES(?, ?)";

	private static final String INSERT_USER_STOCK_SQL =
			"INSERT INTO Stock(ticker, username, " +
							  "quantity, dateBought, dateSold) " +
			"VALUES(?, ?, ?, ?, ?)";

	private static final String GET_USER_SQL =
			"SELECT * FROM User " +
			"WHERE username = ?";

	private static final String GET_USER_STOCKS_SQL =
			"SELECT * FROM Stock " +
			"WHERE username = ?";
	
	private static final String DELETE_USER_SQL =
			"DELETE FROM User " +
			"WHERE username = ?";
	
	private static final String DELETE_USER_STOCKS_SQL =
			"DELETE FROM Stock " +
			"WHERE username = ?";
	
	private static final String DELETE_USER_STOCK_SQL =
			"DELETE FROM Stock " +
			"WHERE username = ? AND stockId = ?";


	/**
	 * Initialize the database tables and establish the connection.
	 * @return Boolean indicating success/failure of operation
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public Boolean init() throws SQLException, ClassNotFoundException {
		if (connection != null) {
			return true;
		}

		Class.forName("org.sqlite.JDBC");

		// establish connection
		connection = DriverManager.getConnection(jdbcURL);
		System.out.println("Connection to SQLite has been established.");

		// create user and stock tables
		String createUserTable =
				  "CREATE TABLE IF NOT EXISTS User ("
				+ "username TEXT UNIQUE,"
				+ "password TEXT NOT NULL,"
			    + "PRIMARY KEY (username)"
				+ ");";

		String createStockTable =
				  "CREATE TABLE IF NOT EXISTS Stock ("
			    + "stockId INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "ticker TEXT,"
				+ "username TEXT,"
				+ "quantity INTEGER,"
			    + "dateBought DATE,"
			    + "dateSold DATE,"
 				+ "FOREIGN KEY(username) REFERENCES User(username)"
				+ ");";

		Statement createTableStatement = connection.createStatement();
		createTableStatement.executeUpdate(createUserTable);
		System.out.println("User table successfully created.");
		createTableStatement.close();
		createTableStatement = connection.createStatement();
		createTableStatement.executeUpdate(createStockTable);
		System.out.println("Stock table successfully created.");
		createTableStatement.close();
		return true;
	}

	/**
	 * Drop all tables of the database and close the connection.
	 * @return Boolean indicating success/failure of operation
	 * @throws SQLException
	 */
	public Boolean destruct() throws SQLException {
		System.out.println();
		if (connection == null) {
			return true;
		}

		// restart a connection to commit all changes
		connection.close();
		connection = DriverManager.getConnection(jdbcURL);

		String deleteUserTable = "DROP TABLE IF EXISTS User";
		String deleteStockTable = "DROP TABLE IF EXISTS Stock";
		Statement deleteTableStatement = connection.createStatement();
		deleteTableStatement.executeUpdate(deleteStockTable);
		System.out.println("Stock table successfully deleted.");
		deleteTableStatement.close();
		deleteTableStatement = connection.createStatement();
		deleteTableStatement.executeUpdate(deleteUserTable);
		System.out.println("User table successfully deleted.");
		deleteTableStatement.close();
		connection.close();
		connection = null;
		return true;
	}

	/**
	 * Insert a new user into the User table.
	 * @param username
	 * @param password
	 * @return Boolean indicating success/failure of operation; if already exists, return false; otherwise return true
	 * @throws SQLException
	 */
	public Boolean insertUser(String username, String password) throws SQLException {
		//check if user already exists
		PreparedStatement getUser = connection.prepareStatement(GET_USER_SQL, Statement.RETURN_GENERATED_KEYS);
		getUser.setString(1, username);
		ResultSet userRSet = getUser.executeQuery();

		if (!userRSet.next()) {
			PreparedStatement insertUser = connection.prepareStatement(INSERT_USER_SQL, Statement.RETURN_GENERATED_KEYS);
			insertUser.setString(1, username);
			insertUser.setString(2, password);
			insertUser.executeUpdate();
			System.out.println("User inserted");
			insertUser.close();
			userRSet.close();
			getUser.close();
			return true;
		}
		userRSet.close();
		getUser.close();
		return false;
	}

	/**
	 * Insert a new stock to the Stock table that belongs to a user.
	 * @param user
	 * @param stock
	 * @return int stockId of the stock inserted; -1 if the insertion failed.
	 * @throws SQLException
	 */
	public int insertUserStock(User user, Stock stock) throws SQLException {
		// add stock as a new row no matter the stock exists or not
		PreparedStatement insertUserStock = connection.prepareStatement(INSERT_USER_STOCK_SQL, Statement.RETURN_GENERATED_KEYS);
		insertUserStock.setString(1, stock.getTicker());
		insertUserStock.setString(2, user.getUsername());
		insertUserStock.setInt(3, stock.getQuantity());
		insertUserStock.setDate(4, new Date(stock.getDateBought().getTime()));
//		insertUserStock.setString(4, stock.getDateBought());
		insertUserStock.setDate(5,
				stock.getDateSold() == null? null : new Date(stock.getDateSold().getTime()));
//		insertUserStock.setString(5, stock.getDateSold());
		insertUserStock.executeUpdate();

		// get the id of the stock
		ResultSet rs = insertUserStock.getGeneratedKeys();
		int result = -1;
		while(rs.next()) {
			result = rs.getInt(1);
		}

		insertUserStock.close();

		return result;
	}

	/**
	 * Get existing user from the database.
	 * @param username
	 * @return User object if the requested user exists;
	 * 		   Null if the the requested user doesn't exist.
	 * @throws SQLException
	 */
	public User getUser(String username) throws SQLException {
		PreparedStatement getUser = connection.prepareStatement(GET_USER_SQL, Statement.RETURN_GENERATED_KEYS);
		getUser.setString(1, username);
		ResultSet userRSet = getUser.executeQuery();
		User user = null;

		if (userRSet.next()) {
			String password = userRSet.getString("password");
			HashMap<Integer, Stock> stocks = new HashMap<>();

			// get user's stocks info
			PreparedStatement getUserStocks = connection.prepareStatement(GET_USER_STOCKS_SQL, Statement.RETURN_GENERATED_KEYS);
			getUserStocks.setString(1, username);
			ResultSet stockRSet = getUserStocks.executeQuery();
			while (stockRSet.next()) {
				int stockId = stockRSet.getInt("stockId");
				String ticker = stockRSet.getString("ticker");
				int quantity = stockRSet.getInt("quantity");
//				String dateBought = stockRSet.getString("dateBought");
				Date dateBoughtSql = stockRSet.getDate("dateBought");
//				String dateSold = stockRSet.getString("dateSold");
				Date dateSoldSql = stockRSet.getDate("dateSold");
				java.util.Date dateBought = new java.util.Date(dateBoughtSql.getTime());
				java.util.Date dateSold = dateSoldSql == null ? null : new java.util.Date(dateSoldSql.getTime());
				stocks.put(stockId, new Stock(stockId, ticker, quantity, username,
						dateBought,
						dateSold));
			}
			stockRSet.close();
			getUserStocks.close();

			user = new User(username, password, stocks);
		}
		userRSet.close();
		getUser.close();
		return user;
	}

	/**
	 * Delete an existing user from the database.
	 * @param username
	 * @return Boolean indicating success/failure of operation
	 * @throws SQLException
	 */
	public Boolean deleteUser(String username) throws SQLException {
		User user = getUser(username);
		if (user == null) {
			return false;
		}
		PreparedStatement deleteUser = connection.prepareStatement(DELETE_USER_SQL, Statement.RETURN_GENERATED_KEYS);
		PreparedStatement deleteUserStocks = connection.prepareStatement(DELETE_USER_STOCKS_SQL, Statement.RETURN_GENERATED_KEYS);
		deleteUser.setString(1, username);
		deleteUser.executeUpdate();
		deleteUserStocks.setString(1, username);
		deleteUserStocks.executeUpdate();
		System.out.println("User deleted");
		deleteUser.close();
		deleteUserStocks.close();
		return true;

	}

	/**
	 * Delete an existing stock from the database.
	 * @param user
	 * @param stockId
	 * @return Boolean indicating success/failure of operation
	 * @throws SQLException
	 */
	public Boolean deleteUserStock(User user, int stockId) throws SQLException {
		PreparedStatement deleteUserStock = connection.prepareStatement(DELETE_USER_STOCK_SQL, Statement.RETURN_GENERATED_KEYS);
		deleteUserStock.setString(1, user.getUsername());
		deleteUserStock.setInt(2, stockId);
		deleteUserStock.executeUpdate();
		deleteUserStock.close();
		return true;
	}
}
