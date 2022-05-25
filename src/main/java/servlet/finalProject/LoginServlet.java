package servlet.finalProject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;
import utils.CSRF;
import utils.Sanitizer;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 * Servlet implementation class HelloWorldServlet
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String USER = "postgres";
    private static final String PWD = "postgres";
    private static final String DRIVER_CLASS = "org.postgresql.Driver";
    private static final String DB_URL = "jdbc:postgresql://localhost/finalproject";

    private static Connection conn;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
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
            System.out.println("Login servlet is valid token");
            response.setContentType("text/html");
            System.out.println("login mail before sanification:" + request.getParameter("email"));
            System.out.println("login password before sanification:" + request.getParameter("password"));
            String email = Sanitizer.sanitizeJsInput(request.getParameter("email"));
            String pwd = Sanitizer.sanitizeJsInput(request.getParameter("password"));

            System.out.println("login mail after sanification:" + email);
            System.out.println("login password after sanification:" + pwd);

            try {
                String getUserInfo = "SELECT password, email FROM users WHERE email=?";
                PreparedStatement pstm = conn.prepareStatement(getUserInfo);
                pstm.setString(1, email);

                ResultSet result = pstm.executeQuery();

                if (result.next()) {
                    if (BCrypt.checkpw(pwd, result.getString("password"))) {
                        System.out.println("Login succeeded!");
                        request.setAttribute("email", result.getString("email"));
                        request.getRequestDispatcher("home.jsp").forward(request, response);

                    } else {
                        System.out.println("Login failed!");
                        request.getRequestDispatcher("login.html").forward(request, response);
                    }

                } else {
                    System.out.println("Login failed!");
                    request.getRequestDispatcher("login.html").forward(request, response);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                request.getRequestDispatcher("login.html").forward(request, response);
            }
        }
    }

}
