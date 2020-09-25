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

// the POST method retrieves shopping cart info from session => no user input involved
// the GET method updates the shopping cart based on user input, but it's stored in session => no sql statement needed

@WebServlet(name = "cartServlet", urlPatterns = "/api/shoppingCart")
public class  cartServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        try {
            // Get a connection from dataSource
            if(request.getSession().getAttribute("cart")!=null){
                //Gson gson = new Gson();
                //String json = gson.toJson(request.getSession().getAttribute("cart"));

                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = (JsonObject)jsonParser.parse((String)request.getSession().getAttribute("cart"));
                out.write(jsonObject.toString());
            }
            else{
                out.write("empty");
            }

            // set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        try{
            String movieId = request.getParameter("id");
            String quantity;
            if(request.getParameter("add")=="true")
                quantity = "1";
            else
                quantity = request.getParameter("quantity");
            // save movie to shopping cart
            Map<String,String> cart;
            if(request.getSession().getAttribute("cart")==null)
                cart = new HashMap<>();
            else {
                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = (JsonObject)jsonParser.parse((String)request.getSession().getAttribute("cart"));

                cart = new Gson().fromJson(
                        jsonObject, new TypeToken<HashMap<String, String>>() {}.getType()
                );
            }

            if(request.getParameter("add").equals("true")) {
                int number = Integer.parseInt(cart.getOrDefault(movieId, "0")) + Integer.parseInt(quantity);
                cart.put(movieId, Integer.toString(number));
            }
            else
                cart.put(movieId, quantity);

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
