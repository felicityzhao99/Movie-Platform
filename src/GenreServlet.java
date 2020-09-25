import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mysql.jdbc.ResultSetMetaData;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
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

@WebServlet(name = "GenreServlet", urlPatterns = "/api/genre")
public class GenreServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    // this GET method retrieves all genre names, no user input involved
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // use connection pooling
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedb");

            Connection dbcon = ds.getConnection();
            Statement statement = dbcon.createStatement();
            // select the genre names
            String query1 = "select group_concat(distinct name) from genres;";
            // Perform the query
            ResultSet rs1 = statement.executeQuery(query1);
            ResultSetMetaData rsmd = (ResultSetMetaData) rs1.getMetaData();
            String result = "";
            if(rs1.next()){
                result = rs1.getString(rsmd.getColumnLabel(1));
            }

            // write JSON string to output
            out.write(result);
            // set response status to 200 (OK)
            response.setStatus(200);
            rs1.close();
            statement.close();
            dbcon.close();
        } catch (Exception e) {
            // write error message to output
            out.write(e.getMessage());
            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();

    }

}
