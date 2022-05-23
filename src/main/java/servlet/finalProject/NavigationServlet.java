package servlet.finalProject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.RSA;
import utils.Sanitizer;

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
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");

        String searchParam = Sanitizer.sanitizeJsInput(request.getParameter("searchParam"));
        String email = Sanitizer.sanitizeJsInput(request.getParameter("email"));
        String pwd = Sanitizer.sanitizeJsInput(request.getParameter("password"));
        String privKeyString = request.getParameter("privKey").replaceAll("[^0-9]", "");

        if (privKeyString == "")
            privKeyString = "0";

        BigInteger privKey = new BigInteger(privKeyString);

        if (request.getParameter("newMail") != null)
            request.setAttribute("content", getHtmlForNewMail(email, pwd));
        else if (request.getParameter("inbox") != null)
            request.setAttribute("content", getHtmlForInbox(email, privKey));
        else if (request.getParameter("sent") != null)
            request.setAttribute("content", getHtmlForSent(email));
        else if (request.getParameter("search") != null)
            request.setAttribute("content", getHtmlForSearchResults(email, searchParam));

        request.setAttribute("email", email);
        request.getRequestDispatcher("home.jsp").forward(request, response);
    }

    protected BigInteger getN(String email) {
        BigInteger n = new BigInteger("0");

        try {
            String sendMail = "SELECT n FROM public_keys WHERE utente =?;";
            PreparedStatement pstm = conn.prepareStatement(sendMail);
            pstm.setString(1, email);

            ResultSet result = pstm.executeQuery();

            while (result.next()) {
                BigDecimal decimalN = result.getBigDecimal("n");
                n = decimalN.toBigInteger();

                return n;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("An error occurred while getting n.");

        return n;
    }

    private String getHtmlForInbox(String email, BigInteger privKey) {
        try {
            String getMailReceived = "SELECT * FROM mail WHERE receiver=? ORDER BY time DESC";
            PreparedStatement pstm = conn.prepareStatement(getMailReceived);
            pstm.setString(1, email);

            ResultSet result = pstm.executeQuery();

            StringBuilder output = new StringBuilder();
            output.append("<div>\r\n");

            final String LDT_PATTERN = "dd.MM.YYYY - HH:mm";
            final DateTimeFormatter LDT_FORMATTER = DateTimeFormatter.ofPattern(LDT_PATTERN);
            
            while (result.next()) {

                RSA rsa = new RSA();

                String body = rsa.decrypt(
                        result.getString("body"),
                        privKey,
                        getN(email));


                String sender = result.getString("sender");
                Timestamp time = result.getTimestamp("time");
                String subject = result.getString("subject");
                System.out.println("sanitized subject" + subject);

                output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">");
                output.append("FROM: " + sender + "\t\tAT:"
                        + LDT_FORMATTER.format(time.toLocalDateTime()));
                output.append("</span>");
                output.append("<br><b>" + subject + "</b>\r\n");
                output.append("<br>" + body);
                output.append("</div>\r\n");

                output.append("<hr style=\"border-top: 2px solid black;\">\r\n");
            }

            output.append("</div>");

            return Sanitizer.getSanitizedHtml(output.toString());

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

                if (result.getString("receiver") == email)
                    mailType = "FROM";

                output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">");
                output.append(mailType + ":\t" + result.getString("receiver") + "\t\tAT:\t"
                        + result.getString("time"));
                output.append("</span>");
                output.append("<br><b>" + result.getString("subject") + "</b>\r\n");
                output.append("<br>" + result.getString("body"));
                output.append("</div>\r\n");

                output.append("<hr style=\"border-top: 2px solid black;\">\r\n");
            }

            output.append("</div>");

            return Sanitizer.getSanitizedHtml(output.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR IN FETCHING INBOX MAILS!";
        }
    }

    private String getHtmlForNewMail(String email, String pwd) {
        String unsafeHtml = "<form id=\"submitForm\" class=\"form-resize\" action=\"SendMailServlet\" method=\"post\">\r\n"
                + "		<input type=\"hidden\" name=\"email\" value=\"" + email + "\">\r\n"
                + "		<input type=\"hidden\" name=\"password\" value=\"" + pwd + "\">\r\n"
                + "		<input class=\"single-row-input\" type=\"email\" name=\"receiver\" placeholder=\"Receiver\" required>\r\n"
                + "		<input class=\"single-row-input\" type=\"text\"  name=\"subject\" placeholder=\"Subject\" required>\r\n"
                + "		<textarea class=\"textarea-input\" name=\"body\" placeholder=\"Body\" wrap=\"hard\" required></textarea>\r\n"
                + "		<input type=\"submit\" name=\"sent\" value=\"Send\">\r\n"
                + "	</form>";
        return Sanitizer.getSanitizedHtml(unsafeHtml);
    }

    private String getHtmlForSent(String email) {
        try {
            String getMailSent = "SELECT * FROM mail WHERE sender=? ORDER BY time DESC";
            PreparedStatement pstm = conn.prepareStatement(getMailSent);
            pstm.setString(1, email);

            ResultSet result = pstm.executeQuery();

            StringBuilder output = new StringBuilder();
            output.append("<div>\r\n");

            while (result.next()) {
                String receiver = result.getString("receiver");
                String time = result.getString("time");
                String subject = result.getString("subject");
                String body = result.getString("body");

                output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">");
                output.append("TO: " + receiver + "  AT:\t" + time);
                output.append("</span>");
                output.append("<br><b>" + subject + "</b>\r\n");
                output.append("<br>" + body);
                output.append("</div>\r\n");

                output.append("<hr style=\"border-top: 2px solid black;\">\r\n");
            }

            output.append("</div>");

            return Sanitizer.getSanitizedHtml(output.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR IN FETCHING INBOX MAILS!";
        }
    }
}
