package servlet.finalProject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.CSRF;
import utils.Encryption;
import utils.Sanitizer;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

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

        Boolean isValidToken = CSRF.validateToken(request, response, "csrfToken");
        if (isValidToken) {
            System.out.println(" navigation servlet is valid token");

            response.setContentType("text/html");

            String searchParam = Sanitizer.sanitizeJsInput(request.getParameter("searchParam"));
            String email = Sanitizer.sanitizeJsInput(request.getParameter("email"));

            String privKeyString = request.getParameter("privKey").replaceAll("[^0-9]", "");

            if (privKeyString == "")
                privKeyString = "0";

            BigInteger privKey = new BigInteger(privKeyString);

            if (request.getParameter("newMail") != null)
                request.setAttribute("content", getHtmlForNewMail(email, response));
            else if (request.getParameter("inbox") != null)
                request.setAttribute("content", getHtmlForInbox(email, privKey));
            else if (request.getParameter("sent") != null)
                request.setAttribute("content", getHtmlForSent(email));
            else if (request.getParameter("search") != null)
                request.setAttribute("content", getHtmlForSearchResults(email, searchParam));

            request.setAttribute("email", email);
            request.getRequestDispatcher("home.jsp").forward(request, response);
        }
    }

    private String getHtmlForInbox(String email, BigInteger receiver_privKey) {
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
                String sender = result.getString("sender");
                Timestamp time = result.getTimestamp("time");
                String subject = result.getString("subject");
                String digest = result.getString("digest");
                String body = result.getString("body");
                int flag = 0;

                if (digest != null)
                    flag = checkDigitalSignature(sender, digest, body);

                BigInteger receiver_n = Encryption.getKeys(email, conn)[1];
                String decryptedBody = Encryption.decryptMessage(
                        body,
                        receiver_privKey,
                        receiver_n);

                output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">");
                output.append("FROM: " + sender + "\t\tAT:"
                        + LDT_FORMATTER.format(time.toLocalDateTime()));
                output.append("</span>");
                output.append("<br><b>" + subject + "</b>\r\n");
                output.append("<br>" + decryptedBody);

                if (flag == 1)
                    output.append("<br> This mail has been digitally signed, and the data it contains is authentic.");
                else if (flag == -1)
                    output.append(
                            "<br> We cannot guarantee the integrity and/or authenticty of the data. Please ask to resend the mail.");

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

    private String getHtmlForNewMail(String email, HttpServletResponse response) {
        String csrfSendMail = "";
        try {
            csrfSendMail = CSRF.getToken();
            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("csrfSendMail", csrfSendMail);
            response.addCookie(cookie);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String unsafeHtml = "<form id=\"submitForm\" class=\"form-resize\" action=\"SendMailServlet\" method=\"post\">\r\n"
                + "     <input type=\"hidden\" name=\"csrfSendMail\" value=\"" + csrfSendMail + "\"/>"
                + "		<input type=\"hidden\" name=\"email\" value=\"" + email + "\">\r\n"
                + "		<input class=\"single-row-input\" type=\"email\" name=\"receiver\" placeholder=\"Receiver\" required>\r\n"
                + "		<input class=\"single-row-input\" type=\"text\"  name=\"subject\" placeholder=\"Subject\" required>\r\n"
                + "		<textarea class=\"textarea-input\" name=\"body\" placeholder=\"Body\" wrap=\"hard\" required></textarea>\r\n"
                + "		<input class=\"single-row-input\" typeo=\"text\" name=\"privKey\" placeholder=\"private key\">\r\n"
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

    // 1 retrieve sender's pub & n from public_keys table
    // 2 decrypt digest from stored in mail using the pub and n
    // 3 return decrypted digest
    // 4 generate own digest
    // 5 compare them if ok decrypt body and return it
    // 6 if not ok getHTML for error

    public int checkDigitalSignature(String sender, String digest, String body) {
        BigInteger[] keys = Encryption.getKeys(sender, conn);
        String generatedDigest = Encryption.generateDigest(body);
        String decryptedDigest = Encryption.decryptMessage(digest, keys[0], keys[1]);

        return (generatedDigest.compareTo(decryptedDigest) == 0) ? 1 : -1;
    }

}
