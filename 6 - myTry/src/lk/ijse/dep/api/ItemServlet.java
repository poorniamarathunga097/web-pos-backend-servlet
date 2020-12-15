package lk.ijse.dep.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
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
        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.addHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.addHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try(Connection connection = cp.getConnection()) {
            Jsonb jsonb = JsonbBuilder.create();
            Item item = jsonb.fromJson(req.getReader(), Item.class);

            if(item.getCode() == null || item.getDescription() == null || item.getQtyOnHand()==0 || item.getPrice().equals(null)){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if(!item.getCode().matches("I\\d{3}") || item.getDescription().trim().isEmpty()){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            PreparedStatement pst = connection.prepareStatement("INSERT INTO Item VALUES (?,?,?,?)");
            pst.setString(1, item.getCode());
            pst.setString(2, item.getDescription());
            pst.setString(3, String.valueOf(item.getQtyOnHand()));
            pst.setString(4, String.valueOf(item.getPrice()));
            if(pst.executeUpdate() > 0){
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }catch(SQLIntegrityConstraintViolationException ex){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }catch (SQLException throwables) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throwables.printStackTrace();
        }catch (JsonbException exp){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");

        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        String code = req.getParameter("item_code");

        resp.setContentType("application/json");
        try (Connection connection = cp.getConnection();) {
            PrintWriter out = resp.getWriter();
            PreparedStatement pst = connection.prepareStatement("SELECT * FROM Item" + ((code != null) ? " WHERE item_code=?" : ""));
            if (code != null) {
                pst.setObject(1, code);
            }
            ResultSet rs = pst.executeQuery();

            List<Item> itemList = new ArrayList<>();

            while (rs.next()) {
                code = rs.getString(1);
                String description = rs.getString(2);
                int qtyOnHand = rs.getInt(3);
                BigDecimal unitPrice = rs.getBigDecimal(4);
                itemList.add(new Item(code, description, qtyOnHand, unitPrice));
            }
            if (code != null && itemList.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                Jsonb jsonb = JsonbBuilder.create();
                out.println(jsonb.toJson(itemList));
                connection.close();
            }

        }  catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");

        String code = req.getParameter("Item_code");
        if(code == null || !code.matches("I\\d{3}")){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try (Connection connection = cp.getConnection();){
            Jsonb jsonb = JsonbBuilder.create();
            Item item = jsonb.fromJson(req.getReader(), Item.class);

            if(item.getCode() == null || item.getDescription() == null){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if(!item.getCode().matches("I\\d{3}") || item.getDescription().trim().isEmpty()){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            PreparedStatement pst = connection.prepareStatement("SELECT * FROM Item WHERE item_code=?");
            pst.setObject(1,code);
            if(pst.executeQuery().next()) {
                pst = connection.prepareStatement("UPDATE Item SET description=?, qty_on_hand=?, unit_price=? WHERE item_code=?");
                pst.setObject(1, item.getDescription());
                pst.setObject(2, item.getQtyOnHand());
                pst.setObject(3, item.getPrice());
                pst.setObject(4,code);
                if (pst.executeUpdate() > 0) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }catch (JsonbException exp){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");

        String code = req.getParameter("item_code");
        if(code == null || !code.matches("I\\d{3}")){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try (Connection connection = cp.getConnection();){

            PreparedStatement pst = connection.prepareStatement("SELECT * FROM Item WHERE item_code=?");
            pst.setObject(1,code);
            if(pst.executeQuery().next()) {
                pst = connection.prepareStatement("DELETE FROM Item WHERE item_code=?");
                pst.setObject(1, code);
                boolean success = pst.executeUpdate() > 0;
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch(SQLIntegrityConstraintViolationException ex){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }catch (SQLException throwables) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throwables.printStackTrace();
        }

    }
}
