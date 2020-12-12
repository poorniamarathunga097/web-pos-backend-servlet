package lk.ijse.dep.api;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.*;

@WebServlet(name = "ItemServlet", urlPatterns = "/items")
public class ItemServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<h1>Item Servlet</h1>");
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dep6", "root", "1234");
                Statement stm = connection.createStatement();
                ResultSet rs = stm.executeQuery("SELECT  * FROM items");
                out.println("<table style='border-collapse: collapse; border: 1px solid black;'>" +
                        "<thead>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "</thead><tbody>");
                while (rs.next()) {
                    String code = rs.getString(1);
                    String description = rs.getString(2);
                    int qtyOnHand = rs.getInt(3);
                    BigDecimal unitPrice = rs.getBigDecimal(4);
                    out.println("<tr>" +
                            "<td>"+ code +"</td>" +
                            "<td>"+ description +"</td>" +
                            "<td>"+ qtyOnHand +"</td>" +
                            "<td>"+ unitPrice +"</td>" +
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
