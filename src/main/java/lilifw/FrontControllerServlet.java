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

import lilifw.utils.ApiMethods;
import lilifw.utils.ControllerMethods;
import lilifw.utils.JsonSerializer;
import lilifw.utils.ModelAndView;
import lilifw.utils.URLMethod;

public class FrontControllerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private transient Map<URLMethod, ControllerMethods> urlToMethods = new HashMap<>();
    private transient Map<URLMethod, ApiMethods> urlToApiMethods = new HashMap<>();

    private transient ApplicationContext ctx;

    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
        Object urlToMethodsAttr = getServletContext().getAttribute("urlToMethods");
        if (urlToMethodsAttr instanceof Map) {
            urlToMethods = (Map<URLMethod, ControllerMethods>) urlToMethodsAttr;
        }
        Object urlToApiMethodsAttr = getServletContext().getAttribute("urlToApiMethods");
        if (urlToApiMethodsAttr instanceof Map) {
            urlToApiMethods = (Map<URLMethod, ApiMethods>) urlToApiMethodsAttr;
        }

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
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String url = uri.substring(contextPath.length());

        if (url.equals("/")) {
            request.setAttribute("annotatedMethods", getUrlToMethods());
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            return;
        }

        request.setAttribute("url", url);

        // Verifier d'abord si la methode existe dans urlToApiMethods (@WebAPI)
        ApiMethods foncApideURL = urlToApiMethods.get(new URLMethod(url, request.getMethod()));
        if (foncApideURL != null) {
            try {
                Class<?> controllerClass = Class.forName(foncApideURL.getController());
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

                Class<?>[] parameterTypes = foncApideURL.getMethod().getParameterTypes();
                Object[] parameters = new Object[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (parameterTypes[i].equals(ApplicationContext.class)) {
                        parameters[i] = ctx;
                    } else {
                        parameters[i] = null;
                    }
                }

                Object result = foncApideURL.getMethod().invoke(controllerInstance, parameters);

                if (foncApideURL.isToJson()) {
                    response.setContentType("application/json; charset=UTF-8");
                    response.getWriter().write(JsonSerializer.serialize(result));
                } else {
                    response.setContentType("text/plain; charset=UTF-8");
                    response.getWriter().write(String.valueOf(result));
                }
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"error\": " + JsonSerializer.serialize(cause.getMessage()) + "}");
            }
        } else {

            // Verifier si la methode existe dans urlToMethods (@UrlMapping)
            ControllerMethods foncDeURL = urlToMethods.get(new URLMethod(url, request.getMethod()));
            if (foncDeURL == null) {
                String prefix = url.substring(0, url.lastIndexOf('/'));
                if (prefix.isEmpty())
                    prefix = "/";

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

            Class<?>[] parameterTypes = foncDeURL.getMethod().getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i].equals(ApplicationContext.class)) {
                    parameters[i] = ctx;
                } else {
                    parameters[i] = null;
                }
            }

            Object result = foncDeURL.getMethod().invoke(controllerInstance, parameters);

            if (result instanceof ModelAndView) {
                ModelAndView mv = (ModelAndView) result;
                for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                    request.setAttribute(entry.getKey(), entry.getValue());
                }
                request.getRequestDispatcher("/" + mv.getView() + ".jsp").forward(request, response);
            } else {
                request.setAttribute("controllerName", foncDeURL.getControllerName());
                request.setAttribute("methodName", foncDeURL.getMethod().getName());
                request.getRequestDispatcher("/route.jsp").forward(request, response);
            }
        }
    }

    public Map<URLMethod, ControllerMethods> getUrlToMethods() {
        return urlToMethods;
    }
}