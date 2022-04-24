package servlet.finalProject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;
import java.time.LocalTime;

import java.time.format.DateTimeFormatter;
import java.util.Date;
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
        // TODO Auto-generated constructor stub
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

		final String LDT_PATTERN = "HH:mm:ss";
		final DateTimeFormatter LDT_FORMATTER
				= DateTimeFormatter.ofPattern(LDT_PATTERN);

		String sender = request.getParameter("email").replace("'", "''");;
		String receiver = request.getParameter("receiver").replace("'", "''");;
		String subject = request.getParameter("subject").replace("'", "''");;
		String body = request.getParameter("body").replace("'", "''");;
		String timestamp = LDT_FORMATTER.format(LocalTime.now());
		
		try (Statement st = conn.createStatement()) {
			st.execute(
				"INSERT INTO mail ( sender, receiver, subject, body, timestamp[2] ) "
				+ "VALUES ( '" + sender + "', '" + receiver + "', '" + subject + "', '" + body + "', '" + timestamp + "' )"
			);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		request.setAttribute("email", sender);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}

}
