import sessionTimeout from "./portfolio/session.js";
import Stock from "./portfolio/stock.js";

/**
 * @param {Response} resp
 * @returns {Promise<object>}
 */
export async function parseResponse(resp) {
	if (sessionTimeout.timerId >= 0) {
		sessionTimeout.start();
	}
	try {
		if (resp.status >= 300 || resp.status < 200) {
			return {
				status: false,
				message: 'Server error.'
			}
		}
		return await resp.json();
	} catch (err) {
		console.error(err.message);
		return {
			status: false,
			message: 'Server error.'
		};
	}
}

/**
 * @returns {Promise<object>}
 */
export async function fetchUserInfo() {
	try {
		let resp = await fetch('/portfolio/value-and-increase', {
			method: 'POST'
		});
		resp = await parseResponse(resp);
		if (!resp.status) {
			window.location.href = '/login.jsp';
			return;
		}
		return resp;
	} catch (err) {
		window.location.href = '/login.jsp';
	}
}

/**
 * @param {object} stock
 * @returns {Stock}
 */
function translateBackendStock(stock) {
	const ret = new Stock(stock.ticker, stock.quantity);
	ret.setDates(stock.dateBought, stock.dateSold);
	ret.setId(stock.stockId);
	return ret;
}


/**
 * @param {object} stock
 * @returns {Promise<Stock[]>}
 */
export async function fetchUserStocks() {
	try {
		let resp = await fetch('/portfolio/stock-list', {
			method: 'POST'
		});
		resp = await parseResponse(resp);
		if (!resp.status) {
			window.location.href = '/login.jsp';
			return;
		}
		return resp.data.map(stock => translateBackendStock(stock));
	} catch (err) {
		window.location.href = '/login.jsp';
	}
}

/**
 * @param {Date} date
 * @returns {string} 
 */
export function toBackendDateString(date) {
	return ('0' + (date.getUTCMonth()+1)).slice(-2) + '/'
		+ ('0' + date.getUTCDate()).slice(-2) + '/'
		+ date.getUTCFullYear();
}