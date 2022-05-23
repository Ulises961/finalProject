package servlet.finalProject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

        String sanitizedSender = Sanitizer.sanitizeJsInput(request.getParameter("email"));
        String sanitizedReceiver = Sanitizer.sanitizeJsInput(request.getParameter("receiver"));
        String sanitizedSubject = Sanitizer.sanitizeJsInput(request.getParameter("subject"));
        String sanitizedBody = Sanitizer.sanitizeJsInput(request.getParameter("body"));
        Timestamp time = Timestamp.valueOf(LocalDateTime.now());

        String encryptedBody = encryptMailBody(sanitizedBody, sanitizedReceiver);

        try {
            String sendMail = "INSERT INTO mail ( sender, receiver, subject, body, time ) VALUES (?, ?, ?, ?, ?);";
            PreparedStatement pstm = conn.prepareStatement(sendMail);
            pstm.setString(1, sanitizedSender);
            pstm.setString(2, sanitizedReceiver);
            pstm.setString(3, sanitizedSubject);
            pstm.setString(4, encryptedBody);
            // pstm.setTimestamp(5, time);
            pstm.setString(5, LDT_FORMATTER.format(time.toLocalDateTime()));

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

    //

    protected String encryptMailBody(String body, String receiver) {
        BigInteger[] keys = getPublicKey(receiver);
        RSA rsa = new RSA();

        BigInteger[] encryptedBody = rsa.encrypt(body, keys[0], keys[1]);
        String newBody = "" + encryptedBody[0];

        for (int i = 1; i < encryptedBody.length; i++)
            newBody += "," + encryptedBody[i];

        return newBody;
    }

    protected BigInteger[] getPublicKey(String receiver) {
        BigInteger[] keys = new BigInteger[2];

        try {
            String sendMail = "SELECT pub,n  FROM public_keys WHERE utente =?;";
            PreparedStatement pstm = conn.prepareStatement(sendMail);
            pstm.setString(1, receiver);

            ResultSet result = pstm.executeQuery();

            while (result.next()) {
                BigDecimal decimalPub = result.getBigDecimal("pub");
                BigInteger pub = decimalPub.toBigInteger();
                keys[0] = pub;

                BigDecimal decimalN = result.getBigDecimal("n");
                BigInteger n = decimalN.toBigInteger();
                keys[1] = n;

                System.out.println("I am returning the pub key");
                return keys;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("An error occurred while getting the pub key");

        return keys;
    }

}
