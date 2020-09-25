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

@WebServlet(name = "Login2Servlet", urlPatterns = "/api/employee_login")
public class Login2Servlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        PrintWriter out = response.getWriter();

        try {
            // use connection pooling
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedb");

            Connection dbcon = ds.getConnection();
            JsonObject jsonObject = new JsonObject();

            // first, check if the employee exists
            String query1 = "select * from employees where email = ?;";

            PreparedStatement statement = dbcon.prepareStatement(query1);
            statement.setString(1,username);

            ResultSet rs1 = statement.executeQuery();
            if(rs1.next()){
                // then, check if password is correct
                String query2 = "select email from employees where email = ? and password = ?;";
                PreparedStatement statement2 = dbcon.prepareStatement(query2);
                statement2.setString(1,username);
                statement2.setString(2,password);
                ResultSet rs2 = statement2.executeQuery();
                if(rs2.next()) {
                    jsonObject.addProperty("status", "success");
                    // set a new session for this user
                    request.getSession().setAttribute("employeeEmail", rs2.getString("email"));
                }
                else{
                    jsonObject.addProperty("message","The password is incorrect!");
                }
                rs2.close();
                statement2.close();
            }
            else{
                jsonObject.addProperty("message","This employee does not exist!");
            }
            if(jsonObject.get("status")==null)
                jsonObject.addProperty("status","fail");

            // write JSON string to output
            out.write(jsonObject.toString());
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

    // check if logged in; output 'yes' if logged in, else 'no'
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        String result = "no";
        try {
            if (request.getSession().getAttribute("employeeEmail") != null)
                result = "yes";
            out.write(result);
        }
        catch (Exception e){
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