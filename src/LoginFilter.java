import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */

/*
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // prevent the browser from caching the pages
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login page if the "userId" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("userId") == null) {
            httpResponse.sendRedirect("/fabflix/index.html");
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("_dashboard.html");
        allowedURIs.add("css_files/dashboard.css");
        allowedURIs.add("js_files/dashboard.js");
        allowedURIs.add("index.html");
        allowedURIs.add("js_files/index.js");
        allowedURIs.add("css_files/index.css");
        allowedURIs.add("api/login");
        allowedURIs.add("api/mobilelogin");
        allowedURIs.add("api/employee_login");
        allowedURIs.add("api/employee_logout");
        allowedURIs.add("api/insert_star");
        allowedURIs.add("api/insert_movie");
    }

    public void destroy() {
        // ignored.
    }

}*/
