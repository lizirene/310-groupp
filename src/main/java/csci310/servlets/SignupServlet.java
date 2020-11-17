package csci310.servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import csci310.Database.DatabaseAPI;
import csci310.model.User;
import csci310.utils.ServletUtils;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {
	static final long serialVersionUID = 1;
	
	private DatabaseAPI dbApi = DatabaseAPI.getInstance();

	public SignupServlet() throws SQLException, ClassNotFoundException {
		super();
	}

	// Overload constructor that accepts externally created userDao instance
	// To support dependency injection.
	public SignupServlet(DatabaseAPI dbApi) throws SQLException, ClassNotFoundException {
		super();
		this.dbApi = dbApi;
	}

	// Response class whose object is to be Jsonified.
	public static class Message {
		String username;
		String password;
		String repeatPassword;

		Message() {}
		Message(String username, String password, String repeatPassword) {
			this.username = username;
			this.password = password;
			this.repeatPassword = repeatPassword;
		}
	}

	public static class Response {
		boolean status;
		Message message;

		Response() {
			this.status = true;
			this.message = new Message();
		}
		Response(boolean status, Message message) {
			this.status = status;
			this.message = message;
		}
		Response(boolean status, String username, String password, String repeatPassword) {
			this.status = status;
			this.message = new Message(username, password, repeatPassword);
		}
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			String username = req.getParameter("username");
			String password = req.getParameter("password");
			String repeatPassword = req.getParameter("repeatPassword");

			Response respObj = new Response();
			// Use RegEx to test username and password
			// Validate username
			Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
			Matcher usernameMatcher = usernamePattern.matcher(username);
			if (!usernameMatcher.find()) {
				respObj.status = false;
				respObj.message.username = "Username should be between 3 and 20 characters and " +
						"only contain alphanumeric characters or underscore";
			}
			else {
				User user = dbApi.getUser(username);
				if (user != null) {
					respObj.status = false;
					respObj.message.username = "An account associated with that username already exists";
				}
			}

			// Validate password
			if (password.length() < 6 || password.length() > 20) {
				respObj.status = false;
				respObj.message.password = "Password should be between 6 and 20 characters";
			}

			// Validate repeat password
			if (!password.equals(repeatPassword)) {
				respObj.status = false;
				respObj.message.repeatPassword = "The password does not match";
			}

			if (respObj.status) {
				// Everything is fine, go ahead to register user
				dbApi.addUser(username, password);
			}
			// Send response
			ServletUtils.sendResponse(resp, respObj);
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
}