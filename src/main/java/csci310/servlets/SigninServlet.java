package csci310.servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import csci310.Database.DatabaseAPI;
import csci310.model.User;
import csci310.utils.ServletUtils;
import csci310.utils.UserLock;

@WebServlet("/signin")
public class SigninServlet extends HttpServlet {
	static final long serialVersionUID = 1;

	private DatabaseAPI dbApi = DatabaseAPI.getInstance();

	// User login attempt locks
	private Map<String, UserLock> attemptLocks = new HashMap<>();

	public SigninServlet() throws SQLException, ClassNotFoundException {
		super();
	}

	// Overload constructor that accepts externally created userDao instance
	// To support dependency injection.
	public SigninServlet(DatabaseAPI dbApi, Map<String, UserLock> attemptLocks) throws SQLException, ClassNotFoundException {
		super();
		this.dbApi = dbApi;
		this.attemptLocks = attemptLocks;
	}

	// Response class whose object is to be Jsonified.
	// Need to be public otherwise JSONObject cannot pick up public getters via reflection.
	public static class Message {
		String username;
		String password;

		Message(String username, String password) {
			this.username = username;
			this.password = password;
		}
	}

	public static class Response {
		boolean status;
		Message message;

		Response(boolean status, Message message) {
			this.status = status;
			this.message = message;
		}
		Response(boolean status, String username, String password) {
			this.status = status;
			this.message = new Message(username, password);
		}
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			String username = req.getParameter("username");
			String password = req.getParameter("password");

			User user = dbApi.getUser(username);

			Response respObj;
			if (user == null) {
				respObj = new Response(
						false,
						"No account was found.",
						null);
			} else if (attemptLocks.containsKey(username) && attemptLocks.get(username).isLocked()) {
				// If the user's account is currently locked
				respObj = new Response(
						false,
						"You have made 3 failed login attempts in the past minute. " +
								"Please try again a minute later.",
						null);
			} else if (!user.equalPassword(password)) {
				respObj = new Response(
						false,
						null,
						"Invalid password.");
				// Record the failed attempt
				// If no lock for this user, then create one
				if (!attemptLocks.containsKey(username)) {
					attemptLocks.put(username, new UserLock(true));
				}
				// Otherwise, put another record
				else {
					attemptLocks.get(username).record();
				}
			} else {
				respObj = new Response(true, null);

				// Initialize user session
				HttpSession session = req.getSession();
				session.setAttribute("user", username);
				session.setAttribute("isLoggedIn", true);

				// Set maximum timeout
				session.setMaxInactiveInterval(120); // Set to 120 seconds

				// Clear any previous failed login attempts, if any
				if (attemptLocks.containsKey(username)) {
					attemptLocks.get(username).clear();
				}
			}
			// Write response
			ServletUtils.sendResponse(resp, respObj);
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
}