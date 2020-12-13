package lk.ijse.dep.api;

import org.apache.commons.dbcp2.BasicDataSource;

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

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");

        resp.addHeader("Access-Control-Allow-Origin","http://localhost:3000");

        resp.setContentType("text/html");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<div>");
            out.println("<h1>Customer Servlet</h1>");
            try {
                Connection connection = cp.getConnection();
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
                out.println("</div>");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
