/**
 * open datepicker by id
 * @param {string} id 
 */
export default function(id) {
	const el = document.getElementById(id);
	const datepicker = M.Datepicker.getInstance(el);
	datepicker.open();
}