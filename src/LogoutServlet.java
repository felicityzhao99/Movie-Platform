import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(name = "LogoutServlet", urlPatterns = "/api/logout")
public class LogoutServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // set the userId attribute to null in session; clear browsing history; clear shopping cart
        request.getSession().setAttribute("userId", null);
        request.getSession().setAttribute("table", null);
        request.getSession().setAttribute("cart",null);
    }

}