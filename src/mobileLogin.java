import com.google.gson.JsonObject;
import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.xml.transform.Result;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;
import sun.security.util.Password;

@WebServlet(name = "mobileLogin", urlPatterns = "/api/mobilelogin")
public class mobileLogin extends HttpServlet {
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

            // first, check if the user exists
            String updateUserName = "select id, password from customers where email = ?;";
            PreparedStatement statement = dbcon.prepareStatement(updateUserName);
            statement.setString(1, username);
            ResultSet rs1 = statement.executeQuery();

            boolean success = false;
            if(rs1.next()){
                // then, check if password is correct in comparison
                String encryptedPassword = rs1.getString("password");
                success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                if (success) {
                    jsonObject.addProperty("status", "success");
                    // set a new session for this user
                    request.getSession().setAttribute("userId", rs1.getString("id"));
                }
                else{
                    jsonObject.addProperty("message","The password is incorrect!");
                }
            }
            else{
                jsonObject.addProperty("message","This user does not exist!");
            }
            if(jsonObject.get("status")==null) {
                jsonObject.addProperty("status", "fail");
            }

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


}