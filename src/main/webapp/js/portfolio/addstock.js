import {parseResponse, toBackendDateString} from "../common.js";
import Stock from "./stock.js";
import {
	adjustXAxisExtremes,
	loadPortfolio,
	plotHistoricalStock,
	plotPortfolioStock,
	updateHistoricalChecked
} from "./highchart.js";

export default {
	data: {
		/** @type {string} */
		type: '',
		/** @type {string} */
		ticker: '',
		/** @type {number} */
		shares: 1,
		/** @type {string} */
		buydate: '',
		/** @type {string} */
		selldate: '',
		/** @type {Array} */
		values: null,
		/** @type {{
         * ticker: string,
         * quantity: string,
         * dateBought: string,
         * dateSold: string
         * }} */
		error: {
			ticker: null,
			quantity: null,
			dateBought: null,
			dateSold: null
		}
	},
	methods: {
		/**
		 * add stock by given stock object
		 * @param e
		 * @returns {Promise<void>}
		 */
		onAddStockFormSubmit: async function (e) {
			e.preventDefault();
			this.loading = true;
			this.addstock.error = {
				ticker: null,
				quantity: null,
				dateBought: null,
				dateSold: null
			};
			this.addstock.ticker = this.addstock.ticker.toUpperCase();
			if (this.addstock.type === 'historical' && this.compare.find(stock => stock.ticker === this.addstock.ticker)) {
				M.toast({html: this.addstock.ticker + ' is already in your historical view.'});
				this.loading = false;
				return;
			}

			try {
				// Get related dates
				const today = new Date();
				const yearAgo = new Date();
				yearAgo.setUTCFullYear(yearAgo.getUTCFullYear() - 1);

				// Send request
				if (this.addstock.type === 'portfolio') {
					const data = new URLSearchParams({
						ticker: this.addstock.ticker,
						quantity: this.addstock.shares,
						dateBought: this.addstock.buydate,
						dateSold: this.addstock.selldate,
						stockType: this.addstock.type
					});
					let resp = await fetch('/portfolio/add-stock', {
						method: 'post',
						body: data,
					});
					resp = await parseResponse(resp);
					if (resp.status) {
						this.updateUserInfo();
						const stock = new Stock(this.addstock.ticker, this.addstock.shares);
						stock.setType("portfolio");
						stock.setDates(this.addstock.buydate, this.addstock.selldate);
						stock.setId(resp.stockId);
						this.stocks.push(stock);
						const stockdata = new URLSearchParams({
							tickerList: JSON.stringify(this.stocks.map(e => e.ticker)),
							startDate: toBackendDateString(yearAgo),
							endDate: toBackendDateString(today),
							frequency: 'daily',
							stockType: this.addstock.type
						});
						let dataResp = await fetch('/portfolio/stock-data', {
							method: 'post',
							body: stockdata,
						});
						dataResp = await parseResponse(dataResp);
						if (dataResp.status) {
							plotPortfolioStock(dataResp.data.aggregated);
							adjustXAxisExtremes(this.stocks.concat(this.compare));
							this.closePopUp('add-stock-modal');
						} else {
							M.toast({html: dataResp.message});
						}
					} else {
						if (typeof (resp.message) === 'string') {
							M.toast({html: resp.message});
						} else {
							this.addstock.error = resp.message;
						}
					}
				} else {
					// plot historical
					const data = new URLSearchParams({
						ticker: this.addstock.ticker,
						quantity: this.addstock.shares,
						dateBought: this.addstock.buydate,
						dateSold: this.addstock.selldate,
						stockType: this.addstock.type
					});
					const stockdata = new URLSearchParams({
						tickerList: JSON.stringify([this.addstock.ticker]),
						startDate: toBackendDateString(yearAgo),
						endDate: toBackendDateString(today),
						frequency: 'daily',
						stockType: this.addstock.type
					});
					let resp = await fetch('/portfolio/add-stock', {
						method: 'post',
						body: data,
					});
					resp = await parseResponse(resp);
					if (resp.status) {
						let dataResp = await fetch('/portfolio/stock-data', {
							method: 'post',
							body: stockdata,
						});
						dataResp = await parseResponse(dataResp);
						if (dataResp.status) {
							this.addstock.values = dataResp.data[this.addstock.ticker];
							const stock = new Stock(this.addstock.ticker, null);
							stock.setDates(this.addstock.buydate, this.addstock.selldate);
							stock.setType("historical");
							stock.setValues(this.addstock.values);
							this.compare.push(stock);
							plotHistoricalStock(stock);
							adjustXAxisExtremes(this.stocks.concat(this.compare));
							this.closePopUp('add-stock-modal');
						} else {
							M.toast({html: dataResp.message});
						}
					} else {
						if (typeof (resp.message) === 'string') {
							M.toast({html: resp.message});
						} else {
							this.addstock.error = resp.message;
						}
					}
				}
			} catch (err) {
				M.toast({html: err.message});
			}
			this.loading = false;
		},
		/**
		 * open add stock form window
		 * @param {string} type
		 */
		openAddStockWindow(type) {
			this.addstock.type = type;
			this.openPopUp('add-stock-modal');
		},
		/**
		 * update the addstock's date
		 * @param {string} key
		 */
		updateAddStockDate(event, key) {
			this.addstock[key] = event.target.value;
		},
		/**
		 * update chart onchange of any stock checked status
		 * @param {Stock} stock
		 */
		updateStockChecked(stock) {
			if (stock.type === "portfolio") {
				loadPortfolio(this.stocks);
			} else if (stock.type === "historical") {
				updateHistoricalChecked(stock);
			}
		},
		/**
		 * update change onchange of all stocks checked status
		 * (given the type ('portfolio' or 'historical')
		 * @param {string} type
		 * @param {boolean} select
		 */
		selectAll(type, select) {
			const list = type === "portfolio" ? this.stocks : this.compare;
			list.forEach(stock => stock.checked = select);
			if (type === "portfolio") {
				loadPortfolio(this.stocks);
			} else if (type === "historical") {
				list.forEach(stock => updateHistoricalChecked(stock, list));
			}
		}
	},
	computed: {
		/**
		 * return button title given type of stock
		 * @returns {string}
		 */
		addStockTitle() {
			return this.addstock.type === 'portfolio' ? 'Add stock to your portfolio' : 'View historical stock';
		},
		/**
		 * return button confirmation given type of stock
		 * @returns {string}
		 */
		addStockConfirm() {
			return this.addstock.type === 'portfolio' ? 'Add Stock' : 'View Stock';
		}
	}
}