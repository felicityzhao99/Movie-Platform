import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;


@WebServlet(name = "cart3Servlet", urlPatterns = "/api/updateCart")
public class cart3Servlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    // the GET method updates the shopping cart based on user input, but it's stored in session => no sql statement needed
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        try{
            String option = request.getParameter("option");   // either be delete or update
            String movieId = request.getParameter("id");      // movieId that needs to be updated
            String quantity = "";
            if(option.equals("update"))
                quantity = request.getParameter("quantity");

            // make update to shopping cart
            Map<String,String> cart;
            if(request.getSession().getAttribute("cart")==null){
                out.write("empty");
                return;
            }
            else {
                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = (JsonObject)jsonParser.parse((String)request.getSession().getAttribute("cart"));

                cart = new Gson().fromJson(
                        jsonObject, new TypeToken<HashMap<String, String>>() {}.getType()
                );
            }

            if(option.equals("update")) {   // update movie quantity
                int number = Integer.parseInt(quantity);
                cart.put(movieId, Integer.toString(number));
            }
            else                                // remove entry from cart
                cart.remove(movieId);

            Gson gson = new Gson();
            String json = gson.toJson(cart);
            request.getSession().setAttribute("cart", json);

            out.write(json);
            response.setStatus(200);
        }
        catch (Exception e){
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(e.toString());
            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();
    }

}