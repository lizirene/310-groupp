package csci310.Database;

import csci310.model.Stock;
import csci310.model.User;

import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseAPI {
    private DatabaseDriver databaseDriver;
    private static DatabaseAPI instance = null;

    public static DatabaseAPI getInstance() throws SQLException, ClassNotFoundException {
    	if (instance == null)
    		instance = new DatabaseAPI();
        return instance;
    }

    /**
     * initialize database
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public DatabaseAPI() throws SQLException, ClassNotFoundException {
        databaseDriver = new DatabaseDriver();
        databaseDriver.init();
    }

    /**
     * clean up DB 
     * @return Boolean indicating success/failure of operation
     * @throws SQLException
     */
    public Boolean clearDB() throws SQLException {
        Boolean result = databaseDriver.destruct();
        instance = null;
        return result;
    }

    /**
     * Return all stocks of a user
     * @param username
     * @return ArrayList of Stock objects belonging to this user
     */
    public ArrayList<Stock> getStockList(String username) throws SQLException {
        User user = databaseDriver.getUser(username);
        if (user == null) {
            System.out.println("User does not exist");
            return null;
        }
        ArrayList<Stock> result = new ArrayList<>(user.getStocks().values());
        return result;
    }


    /**
     * Add a stock to a user's portfolio
     * @param username
     * @param stock
     * @return int stockId of the stock inserted; -1 if the insertion failed.
     */
    public int addStock(String username, Stock stock) throws SQLException {
        User user = databaseDriver.getUser(username);
        if(user == null) {
            System.out.println("User does not exist");
            return -1;
        }
        return databaseDriver.insertUserStock(user, stock);
    }


    /**
     * Delete a stock from a user's portfolio
     * @param username
     * @param stockId
     * @return Boolean indicating success/failure of operation
     */
    public Boolean deleteStock(String username, int stockId) throws SQLException {
        User user = databaseDriver.getUser(username);
        if (user == null) {
            System.out.println("User does not exist");
            return false;
        }
        databaseDriver.deleteUserStock(user, stockId);
        return true;
    }


    /**
     * Add user with empty stock portfolio
     * @param username
     * @param password
     * @return Boolean indicating success/failure of operation
     */
    public Boolean addUser(String username, String password) throws SQLException {
        // Use User model object to hash password
        User user = new User(username, password);
        user.setPasswordHash();
        databaseDriver.insertUser(user.getUsername(), user.getPasswordHash());
        return true;
    }

    /**
     * Add user with empty stock portfolio
     * @param username
     * @return User the corresponding User instance with the given username
     */
    public User getUser(String username) throws SQLException {
        return databaseDriver.getUser(username);
    }

    /**
     * Delete certain user
     * @param username
     * @return Boolean indicating success/failure of operation
     */
    public Boolean deleteUser(String username) throws SQLException {
        return databaseDriver.deleteUser(username);
    }
}
