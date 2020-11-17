import { fetchUserStocks, parseResponse } from "../common.js";
import { adjustXAxisExtremes } from "./highchart.js";

export default {
	data: {
		error: null
	},
	methods: {
		/**
		 * upload csv and update chart
		 * @param e
		 * @returns {Promise<void>}
		 */
		onUploadFormSubmit: async function (e) {
			e.preventDefault();
			this.loading = true;
			this.upload.error = null;
			const data = new FormData(e.target);
			try {
				let resp = await fetch('/portfolio/upload', {
					method: 'post',
					body: data
				});
				resp = await parseResponse(resp);
				if (resp.status) {
					this.updateUserInfo();
					this.stocks = await fetchUserStocks();
					await this.loadPortfolio(this.stocks);
					adjustXAxisExtremes(this.stocks.concat(this.compare));
					this.closePopUp('upload-csv-modal');
				} else {
					this.upload.error = resp.message;
				}
			} catch (err) {
				M.toast({html: err.message});
			}
			this.loading = false;
		}
	}
}