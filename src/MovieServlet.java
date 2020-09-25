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

@WebServlet(name = "MovieServlet", urlPatterns = "/api/movie")
public class MovieServlet extends HttpServlet {

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        String id = request.getParameter("id");
        try {
            // use connection pooling
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedb");

            Connection dbcon = ds.getConnection();
            PreparedStatement statement = dbcon.prepareStatement("select title, year, director from movies where id = ?");
            statement.setString(1,id);
            ResultSet rs1 = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            // retrieve actor information
            if(rs1.next()) {
                String movie_title = "";
                String movie_year = "";
                String movie_director = "";
                String movie_genre = "";
                String movie_actor = "";
                String actor_id = "";
                String movie_rating = "";


                movie_title = rs1.getString("title");
                movie_year = Integer.toString(rs1.getInt("year"));
                movie_director = rs1.getString("director");

                Statement statement2 = dbcon.createStatement();
                String query2 = "select group_concat(n.name order by n.name) from (" +
                        "select name from genres where id in (select genreId from genres_in_movies where movieId = '"
                        + id + "')) as n";
                ResultSet rs2 = statement2.executeQuery(query2);
                ResultSetMetaData rsmd = (ResultSetMetaData) rs2.getMetaData();
                if(rs2.next()){
                    movie_genre = rs2.getString(rsmd.getColumnLabel(1));
                }
                rs2.close();

                String query3 = "select group_concat(n.name) from (select s.name from stars_in_movies "+
                        "as sm, movies as m, stars as s where s.id = sm.starId and sm.movieId = m.id and sm.movieId = '"
                                + id + "' group by s.id order by count(sm.movieId) DESC, s.name) as n;";
                ResultSet rs3 = statement2.executeQuery(query3);
                rsmd = (ResultSetMetaData) rs3.getMetaData();
                if(rs3.next()){
                    movie_actor = rs3.getString(rsmd.getColumnLabel(1));
                }
                rs3.close();

                String query4 = "select group_concat(n.starId) from (select sm.starId from stars_in_movies "+
                        "as sm, movies as m, stars as s where s.id = sm.starId and sm.movieId = m.id and sm.movieId = '"
                        + id + "' group by s.id order by count(sm.movieId) DESC, s.name) as n;";
                ResultSet rs4 = statement2.executeQuery(query4);
                ResultSetMetaData rsmd2 = (ResultSetMetaData) rs4.getMetaData();
                if(rs4.next()){
                    actor_id = rs4.getString(rsmd2.getColumnLabel(1));
                }
                rs4.close();

                String query5 = "select rating from ratings where movieId = '" + id + "';";
                ResultSet rs5 = statement2.executeQuery(query5);
                rsmd = (ResultSetMetaData) rs5.getMetaData();
                if(rs5.next()){
                    movie_rating = rs5.getString(rsmd.getColumnLabel(1));
                }
                rs5.close();
                statement2.close();

                // Create a JsonObject based on the data
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("title", movie_title);
                jsonObject.addProperty("year", movie_year);
                jsonObject.addProperty("director", movie_director);
                jsonObject.addProperty("genres", movie_genre);
                jsonObject.addProperty("stars", movie_actor);
                jsonObject.addProperty("starId", actor_id);
                jsonObject.addProperty("rating", movie_rating);
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
