import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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


@WebServlet(name = "saveTableServlet", urlPatterns = "/api/saveTable")
public class saveTableServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String table_entries = request.getParameter("table");
        // save table entries to session
        request.getSession().setAttribute("table", table_entries);

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            JsonArray jsonArray = new JsonArray();
            if(request.getSession().getAttribute("table")!=null){
                JsonParser jsonParser = new JsonParser();
                jsonArray = (JsonArray) jsonParser.parse((String) request.getSession().getAttribute("table"));
                out.write(jsonArray.toString());
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

}