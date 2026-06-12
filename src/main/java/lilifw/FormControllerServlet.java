package lilifw;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FormControllerServlet extends HttpServlet {
    public static String processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = request.getRequestURI();
        return url;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String url = processRequest(request, response);
        response.getWriter().write(url);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String url = processRequest(request, response);
        response.getWriter().write(url);
    }
}