package servlet.finalProject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import static java.lang.System.out;

/**
 * Servlet implementation class NavigationServlet
 */
@WebServlet("/NavigationServlet")
public class NavigationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String USER = "postgres";
    private static final String PWD = "postgres";
    private static final String DRIVER_CLASS = "org.postgresql.Driver";
    private static final String DB_URL = "jdbc:postgresql://localhost/finalproject";

    private static Connection conn;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public NavigationServlet() {
        super();
    }

    public void init() throws ServletException {
        try {
            Class.forName(DRIVER_CLASS);

            Properties connectionProps = new Properties();
            connectionProps.put("user", USER);
            connectionProps.put("password", PWD);

            conn = DriverManager.getConnection(DB_URL, connectionProps);

            // System.out.println("User \"" + USER + "\" connected to database.");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");

        String searchParam = request.getParameter("searchParam");
        String email = request.getParameter("email");
        String pwd = request.getParameter("password");

        if (request.getParameter("newMail") != null)
            request.setAttribute("content", getHtmlForNewMail(email, pwd));
        else if (request.getParameter("inbox") != null)
            request.setAttribute("content", getHtmlForInbox(email));
        else if (request.getParameter("sent") != null)
            request.setAttribute("content", getHtmlForSent(email));
        else if (request.getParameter("search") != null)
            request.setAttribute("content", getHtmlForSearchResults(email, searchParam));

        request.setAttribute("email", email);
        request.getRequestDispatcher("home.jsp").forward(request, response);
    }

    private String getHtmlForInbox(String email) {
    	try{
        	String getMailReceived = "SELECT * FROM mail WHERE receiver=? ORDER BY time DESC";
        	PreparedStatement pstm = conn.prepareStatement(getMailReceived);
            pstm.setString(1, email);
            
            ResultSet result = pstm.executeQuery(); 

            StringBuilder output = new StringBuilder();
            output.append("<div>\r\n");

            while (result.next()) {
                output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">");
                output.append("FROM:&emsp;" + result.getString("sender") + "&emsp;&emsp;AT:&emsp;" + result.getString("time"));
                output.append("</span>");
                output.append("<br><b>" + result.getString("subject") + "</b>\r\n");
                output.append("<br>" + result.getString("body"));
                output.append("</div>\r\n");

                output.append("<hr style=\"border-top: 2px solid black;\">\r\n");
            }

            output.append("</div>");

            return output.toString();

        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR IN FETCHING INBOX MAILS!";
        }
    }

    private String getHtmlForSearchResults(String email, String searchParam) {
        try (Statement st = conn.createStatement()) {

        	String searchForMail = "SELECT * FROM mail WHERE "
        						   + "( receiver=? OR sender=? ) AND "
        						   + "( subject LIKE ? OR receiver LIKE ?) "
        						   + "ORDER BY time DESC";
        	
        	PreparedStatement pstm = conn.prepareStatement(searchForMail);
            pstm.setString(1, email);
            pstm.setString(2, email);
            pstm.setString(3, '%' + searchParam + '%');
            pstm.setString(4, '%' + searchParam + '%');
            
            ResultSet result = pstm.executeQuery();

            StringBuilder output = new StringBuilder();

            output.append("<div>\r\n");
            output.append("<div>Search results for: " + searchParam + "</div><br>");

            while (result.next()) {
            	String mailType = "TO";
            	
            	if(result.getString("receiver") == email)
            		mailType = "FROM";
            	
                output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">");
                output.append(mailType + ":&emsp;" + result.getString("receiver") + "&emsp;&emsp;AT:&emsp;" + result.getString("time"));
                output.append("</span>");
                output.append("<br><b>" + result.getString("subject") + "</b>\r\n");
                output.append("<br>" + result.getString("body"));
                output.append("</div>\r\n");

                output.append("<hr style=\"border-top: 2px solid black;\">\r\n");
            }

            output.append("</div>");

            return output.toString();

        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR IN FETCHING INBOX MAILS!";
        }
    }

    private String getHtmlForNewMail(String email, String pwd) {
        return "<form id=\"submitForm\" class=\"form-resize\" action=\"SendMailServlet\" method=\"post\">\r\n"
                + "		<input type=\"hidden\" name=\"email\" value=\"" + email + "\">\r\n"
                + "		<input type=\"hidden\" name=\"password\" value=\"" + pwd + "\">\r\n"
                + "		<input class=\"single-row-input\" type=\"email\" name=\"receiver\" placeholder=\"Receiver\" required>\r\n"
                + "		<input class=\"single-row-input\" type=\"text\"  name=\"subject\" placeholder=\"Subject\" required>\r\n"
                + "		<textarea class=\"textarea-input\" name=\"body\" placeholder=\"Body\" wrap=\"hard\" required></textarea>\r\n"
                + "		<input type=\"submit\" name=\"sent\" value=\"Send\">\r\n"
                + "	</form>";
    }

    private String getHtmlForSent(String email) {
        try{
        	String getMailSent = "SELECT * FROM mail WHERE sender=? ORDER BY time DESC";
        	PreparedStatement pstm = conn.prepareStatement(getMailSent);
            pstm.setString(1, email);
            
            ResultSet result = pstm.executeQuery(); 

            StringBuilder output = new StringBuilder();
            output.append("<div>\r\n");

            while (result.next()) {
                output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">");
                output.append("TO:&emsp;" + result.getString("receiver") + "&emsp;&emsp;AT:&emsp;" + result.getString("time"));
                output.append("</span>");
                output.append("<br><b>" + result.getString("subject") + "</b>\r\n");
                output.append("<br>" + result.getString("body"));
                output.append("</div>\r\n");

                output.append("<hr style=\"border-top: 2px solid black;\">\r\n");
            }

            output.append("</div>");

            return output.toString();

        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR IN FETCHING INBOX MAILS!";
        }
    }
}
