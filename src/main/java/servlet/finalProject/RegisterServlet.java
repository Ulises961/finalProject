package servlet.finalProject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;
import utils.RSA;
import utils.RSAKeys;

import java.io.IOException;
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

            //System.out.println("User \"" + USER + "\" connected to database.");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        // The replacement escapes apostrophe special character in order to store it in SQL
        String name = request.getParameter("name");
        String surname = request.getParameter("surname");
        String email = request.getParameter("email");
        String pwd = request.getParameter("password");

        String salt = BCrypt.gensalt(BCryptWorkload);
        String pwdhash = BCrypt.hashpw(pwd, salt);
        RSA rsa = new RSA();
        RSAKeys keys = rsa.generateKeys();
        BigInteger privKey = keys.getE();
        BigInteger pubKey = keys.getD();

        try (Statement st = conn.createStatement()) {
            ResultSet sqlRes = st.executeQuery(
                    "SELECT * "
                            + "FROM users "
                            + "WHERE email='" + email + "'"
            );

            if (sqlRes.next()) {
                System.out.println("Email already registered!");
                request.getRequestDispatcher("register.html").forward(request, response);

            } else {
                st.execute(
                        "INSERT INTO users ( name, surname, email, password ) "
                                + "VALUES ( '" + name + "', '" + surname + "', '" + email + "', '" + pwdhash + "' ); " +
                                "INSERT INTO public_keys(pub,utente) VALUES (" + pubKey + ",'" + email + "')"
                );

                request.setAttribute("email", email);
                request.setAttribute("password", pwdhash);

                request.setAttribute("privKey", privKey);
                System.out.println("Registration succeeded!");
                System.out.println("User private key " + privKey);
                System.out.println("User public key " + pubKey);
                request.getRequestDispatcher("home.jsp").forward(request, response);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            request.getRequestDispatcher("register.html").forward(request, response);
        }
    }

}
