package servlet.finalProject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.RSA;
import utils.RSAKeys;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/DownloadServlet")
public class DownloadServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String filename = "home.jsp";
        RSA rsa = new RSA();
        RSAKeys keys = rsa.generateKeys();
        String privateKey = keys.getD().toString();
        response.setContentType("APPLICATION/OCTET-STREAM");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");


        out.write(privateKey);

        out.close();
    }

}