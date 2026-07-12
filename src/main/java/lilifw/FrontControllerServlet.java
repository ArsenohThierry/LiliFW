package lilifw;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lilifw.utils.ControllerMethods;
import lilifw.utils.ModelAndView;
import lilifw.utils.URLMethod;

public class FrontControllerServlet extends HttpServlet {

    // Map<String,List<Method>> listeMethodesAnnotes = new java.util.HashMap<>();
    // Map<String, ControllerMethods> urlToMethods = new HashMap<>();
    Map<URLMethod, ControllerMethods> urlToMethods;

    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
        urlToMethods = (Map<URLMethod, ControllerMethods>)
            getServletContext().getAttribute("urlToMethods");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String url = uri.substring(contextPath.length());

        if (url == null || url.equals("/")) {
            request.setAttribute("annotatedMethods", getUrlToMethods());
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            return;
        }

        request.setAttribute("url", url);

        ControllerMethods foncDeURL = urlToMethods.get(new URLMethod(url, request.getMethod()));
        if (foncDeURL == null) {
            String prefix = url.substring(0, url.lastIndexOf('/'));
            if (prefix.isEmpty()) prefix = "/";

            Map<URLMethod, ControllerMethods> matchingRoutes = new HashMap<>();
            for (Map.Entry<URLMethod, ControllerMethods> entry : urlToMethods.entrySet()) {
                if (entry.getKey().getUrl().startsWith(prefix)) {
                    matchingRoutes.put(entry.getKey(), entry.getValue());
                }   
            }

            request.setAttribute("notFoundUrl", url);
            request.setAttribute("matchingRoutes", matchingRoutes);
            request.getRequestDispatcher("/route.jsp").forward(request, response);
            return;
        }


    }

    public Map<URLMethod, ControllerMethods> getUrlToMethods() {
        return urlToMethods;
    }

    // public Map<String, ControllerMethods> getUrlToMethods() {
    //     return urlToMethods;
    // }

    // public Map<String, List<Method>> getListeMethodesAnnotes() {
    // return listeMethodesAnnotes;
    // }

}