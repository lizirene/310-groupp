package csci310.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class Stock {
	private int stockId;
	private String ownerUsername;
	private String ticker;
	private int quantity;
	private Date dateBought;
	private Date dateSold;

	/**
	 * Construct a Stock given stock_id, ticker, quantity, ownerUsername, dateBought
	 * @param {int} stock_id
	 * @param {String} ticker
	 * @param {int} quantity
	 * @param {String} ownerUsername
	 * @param {Date} dateBought
	 */
	public Stock(int stock_id, String ticker, int quantity, String ownerUsername, Date dateBought) {
		this.stockId = stock_id;
		this.ticker = ticker.toUpperCase();
		this.ownerUsername = ownerUsername;
		this.dateBought = dateBought;
		this.quantity = quantity;
		this.dateSold = null;
	}

	/**
	 * Construct a Stock given stock_id, ticker, quantity, ownerUsername, dateBought, dateSold
	 * @param {int} stock_id
	 * @param {String} ticker
	 * @param {int} quantity
	 * @param {String} ownerUsername
	 * @param {Date} dateBought
	 * @param {Date} dateSold
	 */
	public Stock(int stock_id, String ticker, int quantity, String ownerUsername, Date dateBought, Date dateSold) {
		this.stockId = stock_id;
		this.ticker = ticker.toUpperCase();
		this.ownerUsername = ownerUsername;
		this.dateBought = dateBought;
		this.quantity = quantity;
		this.dateSold = dateSold;
	}

	/**
	 * Construct a Stock given stock_id, ticker, quantity, ownerUsername, dateBought
	 * @param {int} stock_id
	 * @param {String} ticker
	 * @param {int} quantity
	 * @param {String} ownerUsername
	 * @param {String} dateBought
	 */
	public Stock(int stock_id, String ticker, int quantity, String ownerUsername, String dateBought) {
		this.stockId = stock_id;
		this.ticker = ticker.toUpperCase();
		this.ownerUsername = ownerUsername;
		this.dateBought = Date.from(LocalDate.parse(dateBought).atStartOfDay(ZoneId.systemDefault()).toInstant());
		this.quantity = quantity;
		this.dateSold = null;
	}

	/**
	 * Construct a Stock given stock_id, ticker, quantity, ownerUsername, dateBought, dateSold
	 * @param {int} stock_id
	 * @param {String} ticker
	 * @param {int} quantity
	 * @param {String} ownerUsername
	 * @param {String} dateBought
	 * @param {String} dateSold
	 */
	public Stock(int stock_id, String ticker, int quantity, String ownerUsername, String dateBought, String dateSold) {
		this.stockId = stock_id;
		this.ticker = ticker.toUpperCase();
		this.ownerUsername = ownerUsername;
		this.dateBought = Date.from(LocalDate.parse(dateBought).atStartOfDay(ZoneId.systemDefault()).toInstant());
		this.quantity = quantity;
		this.dateSold = Date.from(LocalDate.parse(dateSold).atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Construct a Stock given ticker, quantity, ownerUsername, dateBought
	 * @param {String} ticker
	 * @param {int} quantity
	 * @param {String} ownerUsername
	 * @param {Date} dateBought
	 */
	public Stock(String ticker, int quantity, String ownerUsername, Date dateBought) {
		this.ticker = ticker.toUpperCase();
		this.ownerUsername = ownerUsername;
		this.dateBought = dateBought;
		this.quantity = quantity;
		this.dateSold = null;
	}

	/**
	 * Construct a Stock given ticker, quantity, ownerUsername, dateBought, dateSold
	 * @param {String} ticker
	 * @param {int} quantity
	 * @param {String} ownerUsername
	 * @param {Date} dateBought
	 * @param {Date} dateSold
	 */
	public Stock(String ticker, int quantity, String ownerUsername, Date dateBought, Date dateSold) {
		this.ticker = ticker.toUpperCase();
		this.ownerUsername = ownerUsername;
		this.dateBought = dateBought;
		this.quantity = quantity;
		this.dateSold = dateSold;
	}

	/**
	 * Construct a Stock given ticker, quantity, ownerUsername, dateBought
	 * @param {String} ticker
	 * @param {int} quantity
	 * @param {String} ownerUsername
	 * @param {String} dateBought
	 */
	public Stock(String ticker, int quantity, String ownerUsername, String dateBought) {
		this.ticker = ticker.toUpperCase();
		this.ownerUsername = ownerUsername;
		this.dateBought = Date.from(LocalDate.parse(dateBought).atStartOfDay(ZoneId.systemDefault()).toInstant());
		this.quantity = quantity;
		this.dateSold = null;
	}

	/**
	 * Construct a Stock given ticker, quantity, ownerUsername, dateBought, dateSold
	 * @param {String} ticker
	 * @param {int} quantity
	 * @param {String} ownerUsername
	 * @param {String} dateBought
	 * @param {String} dateSold
	 */
	public Stock(String ticker, int quantity, String ownerUsername, String dateBought, String dateSold) {
		this.ticker = ticker.toUpperCase();
		this.ownerUsername = ownerUsername;
		this.dateBought = Date.from(LocalDate.parse(dateBought).atStartOfDay(ZoneId.systemDefault()).toInstant());
		this.quantity = quantity;
		this.dateSold = Date.from(LocalDate.parse(dateSold).atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * getter for stockID
	 * @return {int} stockID
	 */
	public int getStockId() {
		return stockId;
	}
	
	/**
	 * setter for stockID
	 * @param {int} stockId
	 */
	public void setStockId(int stockId) {
		this.stockId = stockId;
	}

	/**
	 * getter for ownerUsername
	 * @return {String} ownerUsername
	 */
	public String getOwnerUsername() {
		return this.ownerUsername;
	}

	/**
	 * setter for ownerUsername
	 * @param {String} ownerUsername
	 */
	public void setOwnerUsername(String ownerUsername) {
		this.ownerUsername = ownerUsername;
	}

	/**
	 * getter for ticker
	 * @return {String} ticker
	 */
	public String getTicker() {
		return ticker;
	}

	/**
	 * setter for ticker
	 * @param {String} ticker
	 */
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	/**
	 * getter for quantity
	 * @return {int} quantity
	 */
	public int getQuantity() {
		return quantity;
	}

	/** 
	 * setter for quantity
	 * @param {int} quantity
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	/**
	 * getter for dateBought as Date
	 * @return {Date} dateBought
	 */
	public Date getDateBought() {
		return this.dateBought;
	}

	/**
	 * getter for dateBought as String
	 * @return {String} dateBought
	 */
	public String getDateBoughtString() {
		return this.dateBought.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
	}

	/**
	 * setter for dateBought as Date
	 * @param {Date} dateBought
	 */
	public void setDateBought(Date dateBought) {
		this.dateBought = dateBought;
	}

	/**
	 * setter for dateBought as String
	 * @param {String} dateBought
	 */
	public void setDateBoughtString(String dateBought) {
		this.dateBought = Date.from(LocalDate.parse(dateBought).atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * getter for dateSold as Date
	 * @return {Date} dateSold
	 */
	public Date getDateSold() {
		return this.dateSold;
	}

	/**
	 * getter for dateSold as String
	 * @return {String} dateSold
	 */
	public String getDateSoldString() {return this.dateSold.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();}

	/**
	 * setter for dateSold as Date
	 * @param {Date} dateSold
	 */
	public void setDateSold(Date dateSold) {
		this.dateSold = dateSold;
	}

	/**
	 * setter for dateSold as String
	 * @param {String} dateSold
	 */
	public void setDateSoldString(String dateSold) {this.dateSold = Date.from(LocalDate.parse(dateSold).atStartOfDay(ZoneId.systemDefault()).toInstant());}
	
}
