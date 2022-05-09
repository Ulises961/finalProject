package servlet.finalProject;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

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

            //System.out.println("User \"" + USER + "\" connected to database.");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        String email = request.getParameter("email");
        String pwd = request.getParameter("password");

        System.out.println("The email is: " + "SELECT * "
                + "FROM users "
                + "WHERE email='" + email + "' ");

        try (Statement st = conn.createStatement()) {
            ResultSet sqlRes = st.executeQuery(
                    "SELECT * "
                            + "FROM users "
                            + "WHERE email='" + email + "' "
            );

            if (sqlRes.next()) {

                if (BCrypt.checkpw(pwd, sqlRes.getString(4))) {
                    request.setAttribute("email", sqlRes.getString(3));
                    request.setAttribute("password", sqlRes.getString(4));

                    System.out.println("Login succeeded!");
                    request.setAttribute("content", "");
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
