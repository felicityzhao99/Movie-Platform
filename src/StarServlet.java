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

@WebServlet(name = "StarServlet", urlPatterns = "/api/insert_star")
public class StarServlet extends HttpServlet {
    @Resource(name = "jdbc/masterdb")
    private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String name = request.getParameter("starname");
        String birthyear = request.getParameter("birthyear");
        PrintWriter out = response.getWriter();

        if(request.getSession().getAttribute("employeeEmail")==null){
            out.write("You need to log in as an employee to do this operation!");
            response.setStatus(200);
            return;
        }

        if(birthyear==null || birthyear.equals(""))
            birthyear = "0";

        try {
            // use connection pooling
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/masterdb");

            Connection dbcon = ds.getConnection();

            String query =  "select max(id) from stars;";
            Statement statement = dbcon.createStatement();
            ResultSet rs1 = statement.executeQuery(query);
            int maxId = 0;
            if(rs1.next()){
                maxId = Integer.parseInt(rs1.getString(1).substring(2));
            }
            maxId ++;
            String newId = "nm".concat(Integer.toString(maxId));
            String query2 = "insert into stars (id,name,birthyear) values(?,?,?)";
            PreparedStatement statement2 = dbcon.prepareStatement(query2);
            statement2.setString(1,newId);
            statement2.setString(2,name);
            statement2.setString(3,birthyear);
            statement2.executeUpdate();

            statement2.close();
            out.write("New star added: " + newId);
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