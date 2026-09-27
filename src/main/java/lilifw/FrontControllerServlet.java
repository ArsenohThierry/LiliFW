package lilifw;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import lilifw.utils.ControllerMethods;
import lilifw.utils.ModelAndView;
import lilifw.utils.URLMethod;

public class FrontControllerServlet extends HttpServlet {

    Map<URLMethod, ControllerMethods> urlToMethods;
    private ApplicationContext ctx;

    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
        urlToMethods = (Map<URLMethod, ControllerMethods>)
            getServletContext().getAttribute("urlToMethods");
        try {
            ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        } catch (Exception e) {
            ctx = null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new ServletException(e.getMessage(), e);
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



        Class<?> controllerClass = Class.forName(foncDeURL.getControllerName());
        Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

        Class<?>[] parameterTypes = foncDeURL.getMethode().getParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].equals(ApplicationContext.class)) {
                parameters[i] = ctx;
            } else {
                parameters[i] = null;
            }
        }

        Object result = foncDeURL.getMethode().invoke(controllerInstance, parameters);

        if (result instanceof ModelAndView) {
            ModelAndView mv = (ModelAndView) result;
            for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }
            request.getRequestDispatcher("/" + mv.getView() + ".jsp").forward(request, response);
        } else {
            // Verifier si la methode appelee via le lien est Annote @WebAPI
            if (true) {
                
            }

            request.setAttribute("controllerName", foncDeURL.getControllerName());
            request.setAttribute("methodName", foncDeURL.getMethode().getName());
            request.getRequestDispatcher("/route.jsp").forward(request, response);
        }

        // Handle si C'est une methode REST (@WebAPI)
        

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