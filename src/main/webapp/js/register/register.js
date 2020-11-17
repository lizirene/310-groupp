import {parseResponse} from '../common.js';

let app = new Vue({
  el: '#app',
  data: {
		username: '',
		password: '',
		repeatPassword: '',
		error: {
			username: null,
			password: null,
			repeatPassword: null
		},
		submitting: false
	},
	mounted() {
		document.getElementById('app').style.display = 'block';
	},
	methods: {
		onFormSubmit: async function (e) {
			e.preventDefault();
			this.submitting = true;
			const data = new URLSearchParams(new FormData(e.target));
			try {
				let resp = await fetch('/signup', {
					method: 'post',
					body: data
				});
				resp = await parseResponse(resp);
				if (resp.status) {
					window.location.href = '/login.jsp';
					return;
				} else {
					if (typeof(resp.message) === 'string') {
						M.toast({html: resp.message});
					} else {
						this.error = resp.message;
					}
				}
			} catch (err) {
				M.toast({html: err.message});
			}
			this.submitting = false;
		}
	},

})

export default app;