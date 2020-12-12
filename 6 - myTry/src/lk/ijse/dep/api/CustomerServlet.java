package lk.ijse.dep.api;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "CustomerServlet", urlPatterns = "/customers")
public class CustomerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<h1>Customer Servlet</h1>");
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dep6", "root", "1234");
                Statement stm = connection.createStatement();
                ResultSet rs = stm.executeQuery("SELECT * FROM customer");
                out.println("<table style='border-collapse: collapse; border: 1px solid black;'>" +
                        "<thead>" +
                        "<td>ID</td>" +
                        "<td>NAME</td>" +
                        "<td>ADDRESS</td>" +
                        "</thead><tbody>");
                while (rs.next()) {
                    String id = rs.getString(1);
                    String name = rs.getString(2);
                    String address = rs.getString(3);
                    out.println("<tr>" +
                            "<td>"+ id +"</td>" +
                            "<td>"+ name +"</td>" +
                            "<td>"+ address +"</td>" +
                            "</tr>");
                }
                connection.close();
                out.println("</tbody></table>");

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
