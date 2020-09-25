import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mysql.jdbc.ResultSetMetaData;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;


@WebServlet(name = "cart2Servlet", urlPatterns = "/api/getTitles")
public class cart2Servlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        try{
            // use connection pooling
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedb");

            Connection dbcon = ds.getConnection();
            Statement statement = dbcon.createStatement();

            String movieId = request.getParameter("id");
            String query1 = "select group_concat(title) from movies where id in " + movieId;

            ResultSet rs1 = statement.executeQuery(query1);
            ResultSetMetaData rsmd = (ResultSetMetaData) rs1.getMetaData();

            String result = "empty";
            if(rs1.next()){
                result = rs1.getString(rsmd.getColumnLabel(1));
            }

            out.write(result);
            response.setStatus(200);
            rs1.close();
            statement.close();
            dbcon.close();
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