import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "BrowseServlet", urlPatterns = "/api/browse")
public class BrowseServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    private String[] sort_helper(String s1, String s2) {
        String[] arr = {null, null};
        if (s1 == null || s2 == null) return arr;
        if (s1.equals("10")) arr[0] = "r.rating ASC";
        if (s1.equals("11")) arr[0] = "r.rating DESC";
        if (s1.equals("20")) arr[0] = "m.title ASC";
        if (s1.equals("21")) arr[0] = "m.title DESC";
        if (s2.equals("10")) arr[1] = "r.rating ASC";
        if (s2.equals("11")) arr[1] = "r.rating DESC";
        if (s2.equals("20")) arr[1] = "m.title ASC";
        if (s2.equals("21")) arr[1] = "m.title DESC";
        return arr;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String option = request.getParameter("option");
        String param = request.getParameter("param");
        String sort1 = request.getParameter("sort1");
        String sort2 = request.getParameter("sort2");
        String[] sort_arr = sort_helper(sort1,sort2);
        String limit = request.getParameter("limit");
        String offset = request.getParameter("offset");
        PrintWriter out = response.getWriter();

        // Since the browsing is not related to the user input
        // and all of the states are sent by url parameters
        // we don't need to use Prepared Statement.
        try {
            // use connection pooling
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedb");

            Connection dbcon = ds.getConnection();
            Statement statement = dbcon.createStatement();
            JsonArray jsonArray = new JsonArray();
            String query1 = "";
            // if browse by genre
            if (offset == null) offset = "0";
            if (limit == null) limit = "10";

            Integer limit_int = Integer.parseInt(limit) + 1;
            if(option.equals("genre")){
                query1 = "select gm.movieId from genres_in_movies as gm " +
                        "join genres as g on g.id = gm.genreId and g.name = '" +
                        param + "' join movies as m on m.id = gm.movieId " +
                        "join ratings as r on r.movieId = m.Id order by " +
                        sort_arr[0] + ", " + sort_arr[1] +
                        " limit " + limit_int + " offset " + Integer.parseInt(offset) + ";";
            }
            else{   // otherwise, browse by title initial
                if(param.equals("*")){      // select titles that start with non alpha-numeric values
                    query1 = "select m.id as movieId, m.title from ratings as r " +
                            "join movies as m on m.title regexp '^[^0-9A-Za-z]' and " +
                            "m.id = r.movieId order by " + sort_arr[0] + ", " + sort_arr[1] +
                            " limit " + limit_int + " offset " + Integer.parseInt(offset) + ";";
                }
                else{
                    query1 = "select m.id as movieId, m.title from ratings as r " +
                            "join movies as m on m.title like '" + param + "%' " +
                            "and m.id = r.movieId order by " + sort_arr[0] + ", " + sort_arr[1] +
                            " limit " + limit_int + " offset " + Integer.parseInt(offset) + ";";
                }
            }

            ResultSet rs1 = statement.executeQuery(query1);
            while(rs1.next()){
                String movie_title = "unknown";
                String movie_id= rs1.getString("movieId");
                String movie_year = "unknown";
                String movie_director = "unknown";
                String movie_genre = "unknown";
                String movie_actor = "unknown";
                String actor_id = "";
                String movie_rating = "N.A.";
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
                // retrieve movie rating
                String query6 = "select rating from ratings where movieId = '" + movie_id + "';";
                ResultSet rs6 = statement2.executeQuery(query6);
                if(rs6.next()) {
                    movie_rating = rs6.getString("rating");
                }
                rs6.close();
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
            rs1.close();
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
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
