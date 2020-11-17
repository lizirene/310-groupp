let idCounter = 0;

export default class Stock {
	/**
	 * @param {string} ticker
	 * @param {number} numShares
	 */
	constructor(ticker, numShares) {
		this.id = --idCounter;
		this.ticker = ticker.toUpperCase();
		this.type = '';
		this.numShares = numShares;
		this.checked = true;
		this.values = null;
		this.color = null;
		this.buyDate = null;
		this.sellDate = null;
	}

	setType(type) {
		this.type = type;
	}

	setId(stockId) {
		this.id = stockId;
	}

	setValues(values) {
		this.values = values;
	}

	setColor(color) {
		this.color = color;
	}

	/**
	 * @param {string} buydate
	 * @param {string} selldate
	 */
	setDates(buydate, selldate) {
		this.buyDate = new Date(buydate);
		this.sellDate = new Date(selldate);
	}
}
