import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// server endpoint URL
@WebServlet(name = "AutoCompleteServlet", urlPatterns = "/autocomplete")
public class AutoCompleteServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    private static final long serialVersionUID = 1L;
    private final String[] stop_words = {
            "a","about","an","are","as","at","be","by","com","de","en","for","from","how","i","in","is","it","la","of","on","or",
            "that","the","this","to","was","what","when","where","who","will","with","und","the","www"
    };

    /*
     *
     * Match the query against superheroes and return a JSON response.
     *
     * For example, if the query is "super":
     * The JSON response look like this:
     * [
     * 	{ "value": "Superman", "data": { "heroID": 101 } },
     * 	{ "value": "Supergirl", "data": { "heroID": 113 } }
     * ]
     *
     * The format is like this because it can be directly used by the
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     *
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // use connection pooling
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedb");

            Connection dbcon = ds.getConnection();
            // setup the response json array
            JsonArray jsonArray = new JsonArray();
            String query = request.getParameter("query");

            // return the empty json array if query is null or empty
            if (query == null || query.trim().isEmpty()) {
                response.getWriter().write(jsonArray.toString());
                return;
            }

            String[] arrOfStr = query.trim().split(" ");
            String title = "";
            for(String s: arrOfStr){
                if(!Arrays.stream(stop_words).anyMatch(s::equals))  // ignore stop words
                    title += "+" + s + "* ";
            }

            String q1 = "select id,title from movies where id in ( SELECT entryID FROM ft WHERE MATCH (entry) AGAINST (? IN BOOLEAN MODE)); ";
            PreparedStatement pstatement = dbcon.prepareStatement(q1);
            pstatement.setString(1,title);
            ResultSet rs1 = pstatement.executeQuery();

            int i = 0; // make sure no more than 10 suggestions returned
            while(rs1.next() && i<10) {
                String movieId = rs1.getString("id");
                String movieTitle = rs1.getString("title");
                jsonArray.add(generateJsonObject(movieId, movieTitle));
                i += 1;
            }
            rs1.close();
            pstatement.close();
            dbcon.close();
            response.getWriter().write(jsonArray.toString());
            return;
        } catch (Exception e) {
            System.out.println(e);
            response.sendError(500, e.getMessage());
        }
    }

    /*
     * Generate the JSON Object from hero to be like this format:
     * {
     *   "value": "Iron Man",
     *   "data": { "movieId": "xyz123456" }
     * }
     *
     */
    private static JsonObject generateJsonObject(String movieId, String title) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", title);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieId", movieId);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }


}

