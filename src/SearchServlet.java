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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {
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

    private final String[] stop_words = {
        "a","about","an","are","as","at","be","by","com","de","en","for","from","how","i","in","is","it","la","of","on","or",
        "that","the","this","to","was","what","when","where","who","will","with","und","the","www"
    };

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long TSstartTime = System.nanoTime();

        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String actor = request.getParameter("actor");
        String sort1 = request.getParameter("sort1");
        String sort2 = request.getParameter("sort2");
        String[] sort_arr = sort_helper(sort1,sort2);
        String limit = request.getParameter("limit");
        String offset = request.getParameter("offset");
        PrintWriter out = response.getWriter();

        long TJstartTime = 0;
        long TJendTime = 0;
        try {
            TJstartTime = System.nanoTime();
            // use connection pooling
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedb");

            Connection dbcon = ds.getConnection();
            JsonArray jsonArray = new JsonArray();
            // only query1 can be altered by user input, so we use PreparedStatement for query1
            String query1 = "select m.id, m.title, m.year, m.director from ratings as r " +
                    "inner join movies as m on m.id = r.movieId and ";
            /*
            if(title!=null&&!title.equals(""))
                query1 += "m.title like '%" + title + "%' and ";
            if(year!=null&&!year.equals(""))
                query1 += "m.year = '" + year + "' and ";
            if(director!=null&&!director.equals(""))
                query1 += "m.director like '%" + director + "%' and ";
            if(actor!=null&&!actor.equals(""))
                query1 += "m.id in (select movieId from stars_in_movies where starId in" +
                        "(select id from stars where name like '%" + actor + "%')) and ";
             */
            if(title==null||title.trim().equals("")) {
                title = "%";
                query1 += "m.title like ? and ";
            }
            else {                                                      // if title not empty, do full text search
                //title = "%".concat(title.trim()).concat("%");
                //query1 += "m.title like ? and ";
                String[] arrOfStr = title.trim().split(" ");
                title = "";
                for(String s: arrOfStr){
                    if(!Arrays.stream(stop_words).anyMatch(s::equals))  // ignore stop words
                        title += "+" + s + "* ";
                }
                query1 += "m.id in (select entryID from ft where MATCH (entry) against (? IN BOOLEAN MODE)) and ";
            }

            if(year==null||year.trim().equals("")) {
                year = "%";
                query1 += "m.year like ? and ";
            }
            else
                query1 += "m.year = ? and ";

            if(director==null||director.trim().equals(""))
                director = "%";
            else
                director = "%".concat(director.trim()).concat("%");
            query1 += "m.director like ? and ";

            if(actor==null||actor.trim().equals(""))
                actor = "%";
            else
                actor = "%".concat(actor.trim()).concat("%");
            query1 += "m.id in (select movieId from stars_in_movies where starId in" +
                        "(select id from stars where name like ?)) and ";

            query1 = query1.substring(0,query1.length()-5);
            query1 += " order by " + sort_arr[0] + ", " + sort_arr[1];
            if (offset == null) offset = "0";
            if (limit == null) limit = "10";
            Integer limit_int = Integer.parseInt(limit) + 1;

            query1 += " limit " + limit_int + " offset " + Integer.parseInt(offset) + ";";
            PreparedStatement pstatement = dbcon.prepareStatement(query1);
            pstatement.setString(1,title);
            pstatement.setString(2,year.trim());
            pstatement.setString(3,director);
            pstatement.setString(4,actor);

            ResultSet rs1 = pstatement.executeQuery();

            while(rs1.next()){
                String movie_title = rs1.getString("title");
                String movie_id= rs1.getString("id");
                String movie_year = rs1.getString("year");
                String movie_director = rs1.getString("director");
                String movie_genre = "unknown";
                String movie_actor = "unknown";
                String actor_id = "";
                String movie_rating = "N.A.";
                Statement statement2 = dbcon.createStatement();
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

                // retrieve movie rating and sort if possible
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
            TJendTime = System.nanoTime();

            // write the output jsonArray
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
            pstatement.close();
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

        long TSendTime = System.nanoTime();
        long TS = TSendTime - TSstartTime;
        long TJ = TJendTime - TJstartTime;
        String contextPath = getServletContext().getRealPath("/");
        String filepath = contextPath+"\\search_log.txt";
        System.out.println(filepath);

        File myfile = new File(filepath);
        myfile.createNewFile();

        try {
            FileWriter myWriter = new FileWriter(filepath,true);
            myWriter.write(TS + "," + TJ + "\n");
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

}
