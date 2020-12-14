package lk.ijse.dep.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.dep.model.Customer;
import lk.ijse.dep.model.Item;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "ItemServlet", urlPatterns = "/items")
public class ItemServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("Access-Control-Allow-Origin","http://localhost:3000");
        resp.addHeader("Access-Control-Allow-Headers","Content-Type");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Jsonb jsonb = JsonbBuilder.create();
        Item item = jsonb.fromJson(req.getReader(), Item.class);

        resp.addHeader("Access-Control-Allow-Origin","http://localhost:3000");

        resp.setContentType("application/json");
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try {
            Connection connection = cp.getConnection();
            PreparedStatement pst = connection.prepareStatement("INSERT INTO items VALUES (?,?,?,?)");
            pst.setString(1,item.getCode());
            pst.setString(2,item.getDescription());
            pst.setString(3, String.valueOf(item.getQtyOnHand()));
            pst.setString(4, String.valueOf(item.getPrice()));
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
                ResultSet rs = stm.executeQuery("SELECT  * FROM items");

                List<Item> itemList = new ArrayList<>();

                while (rs.next()) {
                    String code = rs.getString(1);
                    String description = rs.getString(2);
                    int qtyOnHand = rs.getInt(3);
                    BigDecimal unitPrice = rs.getBigDecimal(4);
                    itemList.add(new Item(code,description,qtyOnHand,unitPrice));
                }

                Jsonb jsonb = JsonbBuilder.create();
                out.println(jsonb.toJson(itemList));

                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
