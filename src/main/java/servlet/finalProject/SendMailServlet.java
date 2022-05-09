package servlet.finalProject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Servlet implementation class SendMailServlet
 */
@WebServlet("/SendMailServlet")
public class SendMailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String USER = "postgres";
	private static final String PWD = "postgres";
	private static final String DRIVER_CLASS = "org.postgresql.Driver";
	private static final String DB_URL = "jdbc:postgresql://localhost/finalproject";

	private static Connection conn;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SendMailServlet() {
		super();
	}

	public void init() throws ServletException {
		try {
			Class.forName(DRIVER_CLASS);

			Properties connectionProps = new Properties();
			connectionProps.put("user", USER);
			connectionProps.put("password", PWD);

			conn = DriverManager.getConnection(DB_URL, connectionProps);

			System.out.println("User \"" + USER + "\" connected to database.");

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");

		final String LDT_PATTERN = "HH:mm:ss";
		final DateTimeFormatter LDT_FORMATTER = DateTimeFormatter.ofPattern(LDT_PATTERN);

		String sender = request.getParameter("email");
		String receiver = request.getParameter("receiver");
		String subject = request.getParameter("subject");
		String body = request.getParameter("body");
		String time = LDT_FORMATTER.format(LocalTime.now());

		try (Statement st = conn.createStatement()) {
			st.execute(
					"INSERT INTO mail ( sender, receiver, subject, body, time[2] ) "
							+ "VALUES ( '" + sender + "', '" + receiver + "', '" + subject + "', '" + body + "', '"
							+ time + "' )");

		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println(encryptMail(receiver).intValue());
		request.setAttribute("email", sender);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}

	//

	protected BigInteger encryptMail(String receiver) {

		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery(
					"SELECT pub FROM public_keys WHERE utente ='" + receiver + "'");

			while (rs.next()) {
				BigDecimal decimal = rs.getBigDecimal("pub");
				System.out.println("I am returning the pub key");
				BigInteger pub = decimal.toBigInteger();
				System.out.println("I am returning the pub key");
				return pub;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("An error occurred while getting the pub key");

		return new BigInteger("1");

	}

}
