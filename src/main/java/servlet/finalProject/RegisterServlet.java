package servlet.finalProject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;
import utils.CSRF;
import utils.RSA;
import utils.RSAKeys;
import utils.Sanitizer;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.*;
import java.util.Properties;

/**
 * Servlet implementation class RegisterServlet
 */
@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String USER = "postgres";
    private static final String PWD = "postgres";
    private static final String DRIVER_CLASS = "org.postgresql.Driver";
    private static final String DB_URL = "jdbc:postgresql://localhost/finalproject";
    private static final int BCryptWorkload = 15;

    private static Connection conn;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterServlet() {
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Boolean isValidToken = CSRF.validateToken(request, response, "csrfToken");
        if (isValidToken) {
            System.out.println(" Register servlet is valid token");

            response.setContentType("text/html");

            String name = Sanitizer.sanitizeJsInput(request.getParameter("name"));
            String surname = Sanitizer.sanitizeJsInput(request.getParameter("surname"));
            String email = Sanitizer.sanitizeJsInput(request.getParameter("email"));
            String pwd = Sanitizer.sanitizeJsInput(request.getParameter("password"));

            // GENERATE HASHED PASSWORD
            String salt = BCrypt.gensalt(BCryptWorkload);
            String pwdhash = BCrypt.hashpw(pwd, salt);

            // GENERATE PUBLICK AND PRIVATE KEYS
            RSA rsa = new RSA();
            RSAKeys keys = rsa.generateKeys();
            BigInteger privKey = keys.getD();
            BigInteger pubKey = keys.getE();
            BigInteger n = keys.getN();

            try {
                String isEmailRegistered = "SELECT * FROM users WHERE email=?";
                PreparedStatement pstm = conn.prepareStatement(isEmailRegistered);
                pstm.setString(1, email);

                ResultSet result = pstm.executeQuery();

                if (result.next()) {
                    System.out.println("Email already registered!");
                    request.getRequestDispatcher("register.html").forward(request, response);

                } else {
                    String registerUser = "INSERT INTO users ( name, surname, email, password ) VALUES (?, ?, ?, ?); ";
                    pstm = conn.prepareStatement(registerUser);
                    pstm.setString(1, name);
                    pstm.setString(2, surname);
                    pstm.setString(3, email);
                    pstm.setString(4, pwdhash);

                    int numUpdates = pstm.executeUpdate();

                    if (numUpdates == 1)
                        System.out.println("Registration succeeded!");
                    else
                        System.out.println("Error during registration!");

                    String setPublicKey = "INSERT INTO public_keys(pub, utente, n) VALUES (?, ?, ?)";
                    pstm = conn.prepareStatement(setPublicKey);
                    pstm.setLong(1, Long.parseLong(pubKey.toString()));
                    pstm.setLong(3, Long.parseLong(n.toString()));
                    pstm.setString(2, email);

                    numUpdates = pstm.executeUpdate();

                    if (numUpdates == 1)
                        System.out.println("Public keys set!");
                    else
                        System.out.println("Could not set public keys!");

                    System.out.println("User private key " + privKey);
                    System.out.println("User public key " + pubKey);

                    request.setAttribute("email", email);
                    request.setAttribute("password", pwdhash);

                    // IDK if this has any use so I just commented it
                    // request.setAttribute("privKey", privKey);

                    // GENERATE PRIVATE KEY FILE
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    String filename = "privateKey.txt";
                    response.setContentType("APPLICATION/OCTET-STREAM");
                    response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                    out.write(privKey.toString());
                    out.close();

                    // request.getRequestDispatcher("home.jsp").forward(request, response);
                    // request.getRequestDispatcher("home.jsp").forward(request, response);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                request.getRequestDispatcher("register.html").forward(request, response);
            }
        }
    }

}
