package lilifw;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import lilifw.dto.ApiMethods;
import lilifw.dto.ControllerMethods;
import lilifw.dto.ModelAndView;
import lilifw.dto.URLMethod;
import lilifw.utils.JsonSerializer;

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

        String url = extractUrl(request);
        if (url.equals("/")) {
            handleIndex(request, response);
            return;
        }

        request.setAttribute("url", url);

        ApiMethods apiMethod = urlToApiMethods.get(new URLMethod(url, request.getMethod()));
        if (apiMethod != null) {
            handleApi(apiMethod, request, response);
            return;
        }

        ControllerMethods mvcMethod = urlToMethods.get(new URLMethod(url, request.getMethod()));
        if (mvcMethod == null) {
            handleNotFound(request, response, url);
            return;
        }

        handleMvc(mvcMethod, request, response);
    }

    private String extractUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        return uri.substring(contextPath.length());
    }

    private void handleIndex(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("annotatedMethods", urlToMethods);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    private void handleApi(ApiMethods apiMethod, HttpServletRequest request, HttpServletResponse response) {
        try {
            Object result = invokeControllerMethod(apiMethod.getController(), apiMethod.getMethod());
            renderApiResult(result, apiMethod.isToJson(), response);
        } catch (Exception e) {
            sendApiError(e, response);
        }
    }

    private void renderApiResult(Object result, boolean toJson, HttpServletResponse response) throws Exception {
        if (toJson) {
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write(JsonSerializer.serialize(result));
        } else {
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write(String.valueOf(result));
        }
    }

    private void sendApiError(Exception e, HttpServletResponse response) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        try {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": " + JsonSerializer.serialize(cause.getMessage()) + "}");
        } catch (Exception ignored) {
        }
    }

    private void handleMvc(ControllerMethods mvcMethod, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Object result = invokeControllerMethod(mvcMethod.getControllerName(), mvcMethod.getMethod());

        if (result instanceof ModelAndView) {
            ModelAndView mv = (ModelAndView) result;
            for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }
            request.getRequestDispatcher("/" + mv.getView() + ".jsp").forward(request, response);
        } else {
            request.setAttribute("controllerName", mvcMethod.getControllerName());
            request.setAttribute("methodName", mvcMethod.getMethod().getName());
            request.getRequestDispatcher("/route.jsp").forward(request, response);
        }
    }

    private void handleNotFound(HttpServletRequest request, HttpServletResponse response, String url)
            throws ServletException, IOException {
        String prefix = url.substring(0, url.lastIndexOf('/'));
        if (prefix.isEmpty()) {
            prefix = "/";
        }

        Map<URLMethod, ControllerMethods> matchingRoutes = new HashMap<>();
        for (Map.Entry<URLMethod, ControllerMethods> entry : urlToMethods.entrySet()) {
            if (entry.getKey().getUrl().startsWith(prefix)) {
                matchingRoutes.put(entry.getKey(), entry.getValue());
            }
        }

        request.setAttribute("notFoundUrl", url);
        request.setAttribute("matchingRoutes", matchingRoutes);
        request.getRequestDispatcher("/route.jsp").forward(request, response);
    }

    private Object invokeControllerMethod(String controllerName, Method method) throws Exception {
        Class<?> controllerClass = Class.forName(controllerName);
        Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].equals(ApplicationContext.class)) {
                parameters[i] = ctx;
            } else {
                parameters[i] = null;
            }
        }

        return method.invoke(controllerInstance, parameters);
    }
}