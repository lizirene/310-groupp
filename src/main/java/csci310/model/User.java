package csci310.model;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;

public class User {
	private String username;
	private String passwordHash;
	private Map<Integer, Stock> stocks;
	
	/**
	 * Construct a User given username and password
	 * @param {String} username
	 * @param {String} password
	 */
	public User(String username, String password) {
		this.username = username;
//		this.passwordHash = DigestUtils.sha256Hex(password);
		this.passwordHash = password;
		this.stocks = new HashMap<>();
	}

	/**
	 * Construct a User given username, password, stocks
	 * @param username
	 * @param password
	 * @param stocks
	 */
	public User(String username, String password, HashMap<Integer, Stock> stocks) {
		this.username = username;
//		this.passwordHash = DigestUtils.sha256Hex(password);
		this.passwordHash = password;
		this.stocks = stocks;
	}

	/**
	 * getter for username
	 * @return {String} username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * setter for username
	 * @param {String} username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * getter for hashed password
	 * @return {String} passwordHash
	 */
	public String getPasswordHash() {
		return passwordHash;
	}

	/**
	 * check if password is correct
	 * @param {String} password
	 * @return Boolean indicating if the passwords match
	 */
	public boolean equalPassword(String password) {
		return passwordHash.equals(DigestUtils.sha256Hex(password));
	}

	/**
	 * hash the given password
	 * @param {String} password
	 */
	public void setPasswordHash() {
		this.passwordHash = DigestUtils.sha256Hex(this.passwordHash);
	}

	/**
	 * getter for stock
	 * @param {int} stockId
	 * @return Stock if stockId is found; Otherwise return null
	 */
	public Stock getStock(int stockId) {
		if (this.stocks.containsKey(stockId)) {
			return this.stocks.get(stockId);
		}
		return null;
	}

	/**
	 * getter for stocks
	 * @return {Map<Integer, Stock>} stocks
	 */
	public Map<Integer, Stock> getStocks() {
		return stocks;
	}

	/**
	 * setter for stocks
	 * @param {Map<Integer, Stock>} stocks
	 */
	public void setStocks(Map<Integer, Stock> stocks) {
		this.stocks = stocks;
	}
	
}
