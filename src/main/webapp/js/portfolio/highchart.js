import {parseResponse} from "../common.js";

let chart = null;

/**
 * perform zoom function on chart given type
 * @param {string} type
 */
export function zoom(type) {
	const oldmin = chart.xAxis[0].min;
	const oldmax = chart.xAxis[0].max;
	const scale = (oldmax - oldmin) * 0.1;
	let today = new Date().getTime();
	let yearAgo = window.rangeMin.getTime();
	let newmin = type === 'in' ? oldmin + scale : oldmin - scale;
	let newmax = type === 'in' ? oldmax - scale : oldmax + scale;
	if ((newmax - newmin) / (1000 * 3600 * 24) <= 5) {
		// Do not zoom in if fewer than 5 days
		return;
	}
	newmin = Math.max(yearAgo, newmin);
	newmax = Math.min(today, newmax);
	chart.xAxis[0].setExtremes(newmin, newmax);
}

/**
 * get chart object
 * @param breaks
 * @returns {*}
 */
export default function getChart(breaks) {
	if (chart == null) {
		chart = Highcharts.stockChart('stock-chart', {
				chart: {
					events: {
						load: function() {
							document.getElementById('stock-chart-zoom').style.display = 'block';
						}
					}
				},
				xAxis: {
					type: 'datetime',
					tickInterval: 1,
					breaks
				},
				rangeSelector: {
					buttons: [{
						type: 'day',
						count: 7,
						text: '7d'
					}, {
						type: 'month',
						count: 1,
						text: '1m'
					}, {
						type: 'month',
						count: 3,
						text: '3m'
					}, {
						type: 'month',
						count: 6,
						text: '6m'
					}, {
						type: 'year',
						count: 1,
						text: '1y'
					}],
					selected: 2
				},

				yAxis: {
					labels: {
						formatter: function () {
							return (this.value > 0 ? ' + ' : '') + this.value + '%';
						}
					},
					plotLines: [{
						value: 0,
						width: 2,
						color: 'silver'
					}]
				},

				plotOptions: {
					series: {
						compare: 'percent',
						showInNavigator: true
					}
				},

				tooltip: {
					pointFormat: '<span style="color:{series.color}">{series.name}</span>: <b>{point.y}</b> ({point.change}%)<br/>',
					valueDecimals: 2,
					split: true
				},

				series: [],
			}
		)
	}
	return chart;
}

/**
 * return date object of three months ago
 * @returns {Date}
 */
function threeMonthsAgo() {
	const today = new Date();
	today.setMonth(today.getMonth() - 3);
	return today;
}

/**
 * adjust x axis by the earliest date bought of stocks given
 * @param {Stock[]} stocks
 */
export function adjustXAxisExtremes(stocks) {
	const dates = stocks.map(e => new Date(e.buyDate || 'invalid').getTime())
		.filter(e => !isNaN(e));
	if (dates.length === 0) {
		dates.push(threeMonthsAgo().getTime());
	}
	const startDate = Math.max(dates.reduce((d1, d2) => d1 < d2 ? d1 : d2), window.rangeMin.getTime());
	chart.xAxis[0].setExtremes(startDate, chart.xAxis[0].max);
}

/**
 * add a historical stock line to the chart given stock object
 * @param {Stock} stock
 */
export function plotHistoricalStock(stock) {
	let idx = chart.series.map(e => e.name).indexOf(stock.ticker);
	if (idx === -1) {
		if (stock.color != null) {
			chart.addSeries({
				name: stock.ticker,
				data: stock.values.map(e => [new Date(e.date).getTime(), e.close]),
				color: stock.color,
			});
		} else {
			chart.addSeries({
				name: stock.ticker,
				data: stock.values.map(e => [new Date(e.date).getTime(), e.close]),
			});
			// set color
			stock.setColor(chart.series[chart.series.length - 1].color);
		}
	}
}

/**
 * add a portfolio to the chart given stock values
 * @param values
 */
export function plotPortfolioStock(values) {
	let idx = chart.series.map(e => e.name).indexOf("Portfolio");
	if (idx >= 0) {
		chart.series[idx].setData(values.map(e => [new Date(e.date).getTime(), e.close]));
	} else {
		// no portfolio stock has been added
		chart.addSeries({
			name: "Portfolio",
			data: values.map(e => [new Date(e.date).getTime(), e.close]),
		});
		// if (values.length > 0) {
		//	 chart.rangeSelector.clickButton(5, true);
		// }
	}
}

/**
 * update the check by the given stock's checked status
 * @param {Stock} stock
 */
export function updateHistoricalChecked(stock) {
	if (stock.checked) {
		plotHistoricalStock(stock);
	} else {
		deleteHistoricalStock(stock);
	}
}

/**
 *
 * @param stock
 */
export function deleteHistoricalStock(stock) {
	let idx = chart.series.map(e => e.name).indexOf(stock.ticker);
	if (idx !== -1) {
		chart.series[idx].remove();
	}
}

/**
 * load the user's all portfolio stocks onto chart
 * @param stocks
 * @returns {Promise<void>}
 */
export async function loadPortfolio(stocks) {
	let startDate = new Date();
	startDate.setFullYear(startDate.getFullYear() - 1);
	stocks.forEach((v, idx) => {
		v.setType("portfolio");
	});
	const data = new URLSearchParams({
		tickerList: JSON.stringify(stocks.filter(e => e.checked).map(e => e.ticker)),
		startDate: startDate.toLocaleDateString(),
		endDate: new Date().toLocaleDateString(),
		frequency: 'daily',
		stockType: 'portfolio',
	});
	let resp = await fetch('/portfolio/stock-data', {
		method: 'post',
		body: data,
	});
	resp = await parseResponse(resp);
	if (resp.status) {
		plotPortfolioStock(resp.data.aggregated);
	}
}