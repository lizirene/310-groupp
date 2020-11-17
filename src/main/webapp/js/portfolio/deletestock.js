import { parseResponse } from "../common.js";
import Stock from "./stock.js";
import {adjustXAxisExtremes, loadPortfolio} from "./highchart.js";

export default {
	data: {},
	methods: {
		/**
		 * delete stock by given stock object
		 * @param {Stock} stock 
		 */
		deleteStock: async function(stock) {
			this.loading = true;
			try {
				const data = new URLSearchParams({
					// ticker: stock.ticker,
					stockId : stock.id,
				});

				let resp = await fetch('/portfolio/remove-stock', {
					method: 'post',
					body: data
				});
				resp = await parseResponse(resp);
				this.closePopUp(stock);
				if (resp.status) {
					const index = this.stocks.indexOf(stock);
					if (index >= 0) {
						this.stocks.splice(index, 1);
					}
					this.updateUserInfo();
					await loadPortfolio(this.stocks);
					adjustXAxisExtremes(this.stocks.concat(this.compare));
				} else {
					M.toast({ html: resp.message });
				}
			} catch (err) {
				M.toast({ html: err.message });
			}
			this.loading = false;
		},
	}
}