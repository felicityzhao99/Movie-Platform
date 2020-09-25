import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mysql.jdbc.ResultSetMetaData;

import javax.annotation.Resource;
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
/*
    We are not using this class anymore.
    It was only used for project1 demo.
*/
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    /*
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
            Statement statement = dbcon.createStatement();
            // select the top 20 movies sorted by their ratings
            String query1 = "select movieId,rating from ratings order by rating desc limit 20;";
            // Perform the query
            ResultSet rs1 = statement.executeQuery(query1);

            JsonArray jsonArray = new JsonArray();
            // Iterate through each movie record
            while (rs1.next()) {
                String movie_title = "unknown";
                String movie_id= rs1.getString("movieId");
                String movie_year = "unknown";
                String movie_director = "unknown";
                String movie_genre = "unknown";
                String movie_actor = "unknown";
                String actor_id = "";
                String movie_rating = rs1.getString("rating");
                Statement statement2 = dbcon.createStatement();
                // retrieve title, year and title information
                String query2 = "select title,year,director from movies where id = '"+ movie_id + "';";
                ResultSet rs2 = statement2.executeQuery(query2);
                if(rs2.next()){
                    movie_title = rs2.getString("title");
                    movie_year = Integer.toString(rs2.getInt("year"));
                    movie_director = rs2.getString("director");
                }
                rs2.close();
                // retrieve first three genres
                String query3 = "select group_concat(n.name order by n.name) from (" +
                        "select name from genres where id in (select genreId from genres_in_movies where movieId = '"
                        + movie_id + "') limit 3) as n";
                ResultSet rs3 = statement2.executeQuery(query3);
                ResultSetMetaData rsmd = (ResultSetMetaData) rs3.getMetaData();
                if(rs3.next()){
                    movie_genre = rs3.getString(rsmd.getColumnLabel(1));
                }
                rs3.close();
                // retrieve first three actors
                String query4 = "select group_concat(n.name) from (select s.name from stars_in_movies "+
                        "as sm, movies as m, stars as s where s.id = sm.starId and sm.movieId = m.id and sm.movieId = '"
                        + movie_id + "' group by s.id order by count(sm.movieId) DESC, s.name Limit 3) as n;";
                ResultSet rs4 = statement2.executeQuery(query4);
                rsmd = (ResultSetMetaData) rs4.getMetaData();
                if(rs4.next()){
                    movie_actor = rs4.getString(rsmd.getColumnLabel(1));
                }
                rs4.close();
                String query5 = "select group_concat(n.starId) from (select sm.starId from stars_in_movies "+
                        "as sm, movies as m, stars as s where s.id = sm.starId and sm.movieId = m.id and sm.movieId = '"
                        + movie_id + "' group by s.id order by count(sm.movieId) DESC, s.name Limit 3) as n;";
                ResultSet rs5 = statement2.executeQuery(query5);
                rsmd = (ResultSetMetaData) rs5.getMetaData();
                if(rs5.next()){
                    actor_id = rs5.getString(rsmd.getColumnLabel(1));
                }
                rs5.close();
                statement2.close();
                // Create a JsonObject based on the data
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_genre", movie_genre);
                jsonObject.addProperty("movie_actor", movie_actor);
                jsonObject.addProperty("actor_id", actor_id);
                jsonObject.addProperty("movie_rating", movie_rating);
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
    */
}
