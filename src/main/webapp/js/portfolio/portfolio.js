import Stock from './stock.js';
import addstock from './addstock.js';
import deletestock from './deletestock.js';
import {fetchUserInfo, fetchUserStocks, parseResponse, toBackendDateString} from '../common.js';
import datepicker from './datepicker.js';
import upload from './upload.js';
import sessionTimeout from './session.js';
import getChart, {deleteHistoricalStock, plotHistoricalStock, loadPortfolio, zoom, adjustXAxisExtremes} from "./highchart.js";

/**
 * @param {string} domId
 */
function getStockPopUp(domId) {
	const el = document.getElementById(domId);
	let instance = M.Modal.getInstance(el);
	if (!instance) {
		M.Modal.init(el);
		instance = M.Modal.getInstance(el);
	}
	return instance;
}

const options = {
	el: '#app',
	data: {
		/** @type {string} */
		username: 'Loading...',
		/** @type {number} */
		totalValue: 0,
		/** @type {number} */
		deltaRatio: 0,
		/** @type {string} */
		frequency: 'Daily',
		/** @type {Stock[]} */
		stocks: [],
		/** @type {Stock[]} */
		compare: [],
		/** @type {boolean} */
		loading: false,
		addstock: addstock.data,
		upload: upload.data,
	},
	mounted: function() {
		document.getElementById('app').style.display = 'block';
		const dateelems = document.querySelectorAll('.datepicker');
		M.Datepicker.init(dateelems, {
			format: 'mm/dd/yyyy'
		});

		// Load user info
		this.initStocks();
		sessionTimeout.start();
	},
	methods: {
		/**
		 * open pop up window
		 * @param {Stock|string} item
		 */
		openPopUp(item) {
			const domId = this.idByStock(item);
			getStockPopUp(domId).open();
		},
		/**
		 * close pop up window
		 * @param {Stock|string} item
		 */
		closePopUp(item) {
			const domId = this.idByStock(item);
			getStockPopUp(domId).close();
		},
		/**
		 * delete the given historical stock
		 * @param {Stock} item
		 */
		deleteCompareStock(item) {
			const compareIndex = this.compare.indexOf(item);
			if (compareIndex >= 0) {
				this.closePopUp(item);
				this.compare.splice(compareIndex, 1);
				adjustXAxisExtremes(this.compare);
			}
			// delete in chart
			deleteHistoricalStock(item);
		},
		/**
		 * return corresponding HTML id by given stock
		 * @param {Stock|string} stock
		 */
		idByStock(stock) {
			if (typeof(stock) === 'string')
				return stock;
			return 'delete-stock-' + stock.id;
		},
		/**
		 * update user's info (username, value, ratio)
		 * @returns {Promise<void>}
		 */
		async updateUserInfo() {
			const resp = await fetchUserInfo();
			this.username = resp.username;
			this.totalValue = resp.totalValue;
			this.deltaRatio = resp.increase * 100;
		},
		/**
		 * load s&p 500
		 * @returns {Promise<void>}
		 */
		async loadSP500() {
			let startDate = new Date();
			let current = new Date();
			startDate.setFullYear(startDate.getFullYear() - 1);
			const data = new URLSearchParams({
				tickerList: JSON.stringify(['SPY']),
				startDate: startDate.toLocaleDateString(),
				endDate: current.toLocaleDateString(),
				frequency: 'daily',
				stockType: 'historical',
			});
			let resp = await fetch('/portfolio/stock-data', {
				method: 'post',
				body: data,
			});
			resp = await parseResponse(resp);
			if (resp.status) {
				const stock = new Stock("SPY", null);
				stock.setType("historical");
				stock.setValues(resp.data["SPY"]);
				/** @type {Date[]} */
				const tradingDays = resp.data["SPY"].map(i => new Date(i.date));
				const breaks = [];
				window.rangeMin = tradingDays[0];
				for (let i = 1; i < tradingDays.length; i++) {
					const yesterday = new Date(tradingDays[i-1]);
					const today = new Date(tradingDays[i]);
					if (yesterday.setDate(yesterday.getDate() + 1) === today.getTime()) {
						continue;
					}
					breaks.push({
						from: yesterday.getTime(),
						to: today.getTime()
					});
				}
				getChart(breaks);
				this.compare.push(stock);
				plotHistoricalStock(stock);
				adjustXAxisExtremes(this.stocks.concat(this.compare));
			}
		},
		/**
		 * initialize stocks (including
		 *	  - update user's info
		 *	  - fetch the user's portfolio stocks
		 *	  - adjust x axis by earliest date bought
		 * @returns {Promise<void>}
		 */
		async initStocks() {
			const prevloading = this.loading;
			this.loading = true;
			this.updateUserInfo();
			this.stocks = (await Promise.all([this.loadSP500(), fetchUserStocks()]))[1];
			await this.loadPortfolio(this.stocks);
			adjustXAxisExtremes(this.stocks);
			this.loading = prevloading;
		},
		...addstock.methods,
		...deletestock.methods,
		...upload.methods,
		openDatePicker: datepicker,
		loadPortfolio,
		zoom,
		dateString: toBackendDateString,
		/**
		 * return date if valid
		 * @param {Date} date 
		 * @returns {boolean}
		 */
		validDate(date) {
			return date && !isNaN(date.getTime());
		}
	},
	computed: {
		...addstock.computed
	}
};

const app = new Vue(options);
export default app;