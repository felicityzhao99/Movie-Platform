import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jdk.nashorn.internal.parser.JSONParser;
import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/pay")
public class PaymentServlet extends HttpServlet {
    @Resource(name = "jdbc/masterdb")
    private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String firstName = request.getParameter("first_name");
        String lastName = request.getParameter("last_name");
        String creditCard = request.getParameter("credit_card");
        String daySelect = request.getParameter("day_select");
        String monthSelect = request.getParameter("month_select");
        String yearSelect = request.getParameter("year_select");
        String expiration = yearSelect + "-" + monthSelect + "-" + daySelect;
        PrintWriter out = response.getWriter();

        try {
            // use connection pooling
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/masterdb");

            Connection dbcon = ds.getConnection();
            //Statement statement = dbcon.createStatement();
            JsonObject jsonObject = new JsonObject();

            // first, check if the user and credit card match
            String updateUserCredit = "select cc.id from creditcards as cc where cc.firstName = ? and " +
                    "cc.lastName = ? and cc.id = ?;";
            PreparedStatement statement = dbcon.prepareStatement(updateUserCredit);
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3,creditCard);
            ResultSet rs1 = statement.executeQuery();
            if(rs1.next()){
                // then, check if the expiration date is correct
                String updateExpirationDate = "select cc.id from creditcards as cc where cc.expiration = ?;";
                PreparedStatement statement2 = dbcon.prepareStatement(updateExpirationDate);
                statement2.setString(1, expiration);
                ResultSet rs2 = statement2.executeQuery();
                if(rs2.next()) {
                    jsonObject.addProperty("status", "success");
                    // set a new session for this credit card
                    request.getSession().setAttribute("creditId", rs2.getString("id"));
                    //  get userId from userId in session
                    String userId = (String)request.getSession(false).getAttribute("userId");
                    // get movieId from cart in session
                    String json_data = (String) request.getSession(false).getAttribute("cart");
                    JsonParser jsonParser = new JsonParser();
                    JsonObject jo = (JsonObject)jsonParser.parse(json_data);
                    //current date
                    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                    int day = calendar.get(Calendar.DATE);
                    //Note: +1 the month for current month
                    int month = calendar.get(Calendar.MONTH) + 1;
                    int year = calendar.get(Calendar.YEAR);
                    String saleDate = year + "-" + month + "-" + day;
                    int rows = 0; //insert rows #
                    for(Iterator it = jo.keySet().iterator(); it.hasNext();) {
                        String movieId = (String) it.next();
                        //This is not related to user input and we don't need to use PreparedStatement.
                        String query3 = "insert into sales (customerId, movieId, saleDate) values " +
                                "('" + userId + "', '" + movieId + "', '" + saleDate + "');";
                        Statement statement3 = dbcon.createStatement();
                        rows += statement3.executeUpdate(query3);
                        statement3.close();
                    }
                    //System.out.println(rows);
                }
                else{
                    jsonObject.addProperty("message",
                            "The expiration date is incorrect! Please re-select.");
                }
                rs2.close();
                statement2.close();
            }
            else{
                jsonObject.addProperty("message",
                        "This user or credit card does not match! Please re-enter.");
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


}
