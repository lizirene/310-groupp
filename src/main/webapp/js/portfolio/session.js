/**
 * @type {number} Session time out in ms.
 */
const TIME_OUT = 120 * 1000;

/**
 * SessionTimer Object to track
 * how much time elapsed and which page to redirect afterwards
 */
class SessionTimer {
	constructor() {
		this.timerId = -1;
	}

	redirect() {
		window.location.href = '/login.jsp';
	}

	stop() {
		if (this.timerId >= 0)
			clearTimeout(this.timerId);
	}

	start() {
		this.stop();
		this.timerId = setTimeout(() => { this.redirect(); }, TIME_OUT);
	}
}

const sessionTimeout = new SessionTimer();

export default sessionTimeout;