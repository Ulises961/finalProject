<%@ page import="utils.CSRF" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="ISO-8859-1"/>
    <title>Login</title>
    <%
        String csrfToken = CSRF.getToken();
        jakarta.servlet.http.Cookie cookie = new
                jakarta.servlet.http.Cookie("csrfToken", csrfToken);
        response.addCookie(cookie);
    %>
</head>
<body>
<form action="LoginServlet" method="post">
    <input type="hidden" name="csrfToken" value="<%= csrfToken %>"/>
    <table>
        <tr>
            <td><font face="verdana" size="2px">Email address:</font></td>
            <td><input name="email" required type="text"/></td>
        </tr>
        <tr>
            <td><font face="verdana" size="2px">Password:</font></td>
            <td><input name="password" required type="password"/></td>
        </tr>
    </table>
    <input name="inbox" type="hidden"/>
    <input type="submit" value="Login"/>
</form>
<p>Or <a href="register.jsp">register</a></p>
</body>
</html>
