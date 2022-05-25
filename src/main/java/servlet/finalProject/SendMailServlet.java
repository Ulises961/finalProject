package servlet.finalProject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Properties;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.CSRF;
import utils.Encryption;
import utils.Sanitizer;

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
        Boolean isValidToken = CSRF.validateToken(request, response, "csrfSendMail");
        if (isValidToken) {
            System.out.println(" Send mail servlet is valid token");

            response.setContentType("text/html");


            String sanitizedSender = Sanitizer.sanitizeJsInput(request.getParameter("email"));
            String sanitizedReceiver = Sanitizer.sanitizeJsInput(request.getParameter("receiver"));
            String sanitizedSubject = Sanitizer.sanitizeJsInput(request.getParameter("subject"));
            String sanitizedBody = Sanitizer.sanitizeJsInput(request.getParameter("body"));
            Timestamp time = Timestamp.valueOf(LocalDateTime.now());

            String sanitizedPrivKey = request.getParameter("privKey").replaceAll("[^0-9]", "");

            String encryptedBody = Encryption.encryptMailBody(sanitizedBody, sanitizedReceiver, conn);
            String digest = null;

            if (sanitizedPrivKey != "") {
                digest = Encryption.encryptDigest(encryptedBody, sanitizedSender, sanitizedPrivKey, conn);
            }
            
            try {
                String sendMail = "INSERT INTO mail ( sender, receiver, subject, body, time, digest ) VALUES (?, ?, ?, ?, ?, ?);";
                PreparedStatement pstm = conn.prepareStatement(sendMail);
                pstm.setString(1, sanitizedSender);
                pstm.setString(2, sanitizedReceiver);
                pstm.setString(3, sanitizedSubject);
                pstm.setString(4, encryptedBody);
                pstm.setTimestamp(5, time);
                pstm.setString(6, digest);

                int numUpdates = pstm.executeUpdate();

                if (numUpdates == 1)
                    System.out.println("Email succesfully sent!");
                else
                    System.out.println("Could not send email!");

            } catch (SQLException e) {
                System.out.println("Could not send email!");
                e.printStackTrace();
            }

            request.setAttribute("email", sanitizedSender);
            request.getRequestDispatcher("home.jsp").forward(request, response);
        }

    }

    //

}
