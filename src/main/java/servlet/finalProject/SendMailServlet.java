package servlet.finalProject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");

		String sender = request.getParameter("email");
		String receiver = request.getParameter("receiver");
		String subject = request.getParameter("subject");
		String body = request.getParameter("body");
		// String time = LDT_FORMATTER.format(LocalTime.now());
		Timestamp time = Timestamp.valueOf(LocalDateTime.now());

		try (Statement st = conn.createStatement()) {
			st.execute(
				"INSERT INTO mail ( sender, receiver, subject, body, time ) "
				+ "VALUES ( '" + sender + "', '" + receiver + "', '" + subject + "', '" + body + "', '" + time + "' )"
			);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		request.setAttribute("email", sender);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}

}
