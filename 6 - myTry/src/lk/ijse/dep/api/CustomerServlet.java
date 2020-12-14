package lk.ijse.dep.api;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.LineInputStream;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.dep.model.Customer;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "CustomerServlet", urlPatterns = "/customers")
public class CustomerServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("Access-Control-Allow-Origin","http://localhost:3000");
        resp.addHeader("Access-Control-Allow-Headers","Content-Type");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Jsonb jsonb = JsonbBuilder.create();
        Customer customer = jsonb.fromJson(req.getReader(), Customer.class);

        resp.addHeader("Access-Control-Allow-Origin","http://localhost:3000");

        resp.setContentType("application/json");
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try {
            Connection connection = cp.getConnection();
            PreparedStatement pst = connection.prepareStatement("INSERT INTO customer VALUES (?,?,?)");
            pst.setString(1,customer.getId());
            pst.setString(2,customer.getName());
            pst.setString(3,customer.getAddress());
            boolean success = pst.executeUpdate()>0;

            if(success){
                resp.getWriter().println(jsonb.toJson(true));
            }else{
                resp.getWriter().println(jsonb.toJson(false));
            }
            connection.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            resp.getWriter().println(jsonb.toJson(false));
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");

        resp.addHeader("Access-Control-Allow-Origin","http://localhost:3000");

        resp.setContentType("application/json");
        try (PrintWriter out = resp.getWriter()) {
            try {
                Connection connection = cp.getConnection();
                Statement stm = connection.createStatement();
                ResultSet rs = stm.executeQuery("SELECT * FROM customer");

                List<Customer> customerList = new ArrayList<>();

                while (rs.next()) {
                    String id = rs.getString(1);
                    String name = rs.getString(2);
                    String address = rs.getString(3);
                    customerList.add(new Customer(id,name,address));
                }

                Jsonb jsonb = JsonbBuilder.create();
                out.println(jsonb.toJson(customerList));

                connection.close();


            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
