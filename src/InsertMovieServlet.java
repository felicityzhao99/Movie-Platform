import com.google.gson.JsonObject;
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

@WebServlet(name = "InsertMovieServlet", urlPatterns = "/api/insert_movie")
public class InsertMovieServlet extends HttpServlet {
    @Resource(name = "jdbc/masterdb")
    private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String title = request.getParameter("title");
        String director = request.getParameter("director");
        String year = request.getParameter("year");
        String genre = request.getParameter("genre");
        String star = request.getParameter("star");
        PrintWriter out = response.getWriter();

        if(request.getSession().getAttribute("employeeEmail")==null){
            out.write("You need to log in as an employee to do this operation!");
            response.setStatus(200);
            return;
        }

        String result = "MySQL server error: please try again";
        try {
            // use connection pooling
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/masterdb");

            Connection dbcon = ds.getConnection();
            String query =  "select add_movie(?,?,?,?,?);";
            PreparedStatement statement = dbcon.prepareStatement(query);
            statement.setString(1,title);
            statement.setString(2,year);
            statement.setString(3,director);
            statement.setString(4,genre);
            statement.setString(5,star);

            ResultSet rs1 = statement.executeQuery();
            if(rs1.next()){
               result = rs1.getString(1);
            }
            out.write(result);

            if(!result.equals("Duplicated movie")) {
                // also need to update ft table for full text search
                String query2 = "INSERT INTO ft (select id,title from movies where title = ? and year = ? and director = ?);";
                PreparedStatement statement1 = dbcon.prepareStatement(query2);
                statement1.setString(1, title);
                statement1.setString(2, year);
                statement1.setString(3, director);
                statement1.executeUpdate();
                statement1.close();
            }

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