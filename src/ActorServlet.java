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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "ActorServlet", urlPatterns = "/api/actor")
public class ActorServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json"); // Response mime type
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        String id = request.getParameter("id");
        try {
            // use connection pooling
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedb");

            // Get a connection from dataSource
            Connection dbcon = ds.getConnection();
            //Statement statement = dbcon.createStatement();
            // select the actor name and year of birth by id
            String updateActor = "select name, birthYear from stars where id = ?";
            PreparedStatement statement = dbcon.prepareStatement(updateActor);
            statement.setString(1,id);
            // Perform the query
            ResultSet rs1 = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            // retrieve actor information
            if(rs1.next()) {
                String name = rs1.getString("name");
                String birthYear = "unknown";
                if(rs1.getString("birthYear")!=""&&rs1.getString("birthYear")!=null)
                    birthYear = rs1.getString("birthYear");
                String movies = "unknown";
                String movie_id = "";
                // select the movies in which the actor has performed
                Statement statement2 = dbcon.createStatement();
                String query2 = "select group_concat(m.id order by m.year DESC, m.title) from stars_in_movies as sm " +
                        "join movies as m on sm.movieId = m.id and sm.starId = '" + id +"';";
                ResultSet rs2 = statement2.executeQuery(query2);
                if(rs2.next()){
                    movie_id = rs2.getString(((ResultSetMetaData) rs2.getMetaData()).getColumnLabel(1));
                }
                rs2.close();
                String query3 = "select group_concat(m.title order by m.year DESC, m.title) from stars_in_movies as sm"+
                        " join movies as m on sm.movieId = m.id and sm.starId = '" + id +"';";
                ResultSet rs3 = statement2.executeQuery(query3);
                if(rs3.next()){
                    movies = rs3.getString(((ResultSetMetaData) rs3.getMetaData()).getColumnLabel(1));
                }
                rs3.close();
                statement2.close();

                // Create a JsonObject based on the data
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", name);
                jsonObject.addProperty("birthYear", birthYear);
                jsonObject.addProperty("movies", movies);
                jsonObject.addProperty("movie_id", movie_id);
                jsonArray.add(jsonObject);
            }

            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
            rs1.close();
            statement.close();
            dbcon.close();
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
