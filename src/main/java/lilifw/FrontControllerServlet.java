package lilifw;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import lilifw.annotation.Auth;
import lilifw.dto.ApiMethods;
import lilifw.dto.ControllerMethods;

import lilifw.dto.ModelAndView;
import lilifw.dto.Session;
import lilifw.dto.URLMethod;
import lilifw.dto.MapFile;
import lilifw.utils.JsonSerializer;
import lilifw.utils.ModelBinder;

@MultipartConfig
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
        } catch (ServletException | IOException e) {
            // On laisse passer telle quelle : Tomcat affichera sa page d'erreur
            // (et reconnait notamment ClientAbortException, qui est un IOException).
            throw e;
        } catch (Exception e) {
            // On ne "consomme" pas l'exception : Tomcat affiche son propre ecran.
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ServletException | IOException e) {
            // On laisse passer telle quelle : Tomcat affichera sa page d'erreur
            // (et reconnait notamment ClientAbortException, qui est un IOException).
            throw e;
        } catch (Exception e) {
            // On ne "consomme" pas l'exception : Tomcat affiche son propre ecran.
            throw new ServletException(e);
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

    private boolean isAuthenticated(HttpServletRequest request) {
        String authSessionAttribut = (String) getServletContext().getAttribute("authSessionParameter");
        if (authSessionAttribut == null || authSessionAttribut.isEmpty()) {
            authSessionAttribut = "userAuthSession";
        }

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            return false;
        }

        Object value = httpSession.getAttribute(authSessionAttribut);
        if (value == null) {
            return false;
        }

        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private boolean hasPermission(Method method, HttpServletRequest request) {
        Auth auth = method.getAnnotation(Auth.class);

        // Pas de @Auth 
        if (auth == null) {
            return true;
        }

        // @Auth : il faut au minimum etre connecte.
        if (!isAuthenticated(request)) {
            return false;
        }

        String requiredRole = auth.role();

        // @Auth sans role : tout utilisateur connecte suffit.
        if (requiredRole == null || requiredRole.isBlank()) {
            return true;
        }

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            return false;
        }

        String roleSessionAttribute = (String) getServletContext().getAttribute("roleSessionParameter");
        if (roleSessionAttribute == null || roleSessionAttribute.isBlank()) {
            roleSessionAttribute = "role";
        }

        Object sessionRole = httpSession.getAttribute(roleSessionAttribute);
        return matchesRole(sessionRole, requiredRole);
    }

    private boolean matchesRole(Object sessionRole, String requiredRole) {
        if (sessionRole == null || requiredRole == null) {
            return false;
        }

        Set<String> userRoles = extractRoles(sessionRole);

        // L'annotation peut contenir plusieurs roles : @Auth(role = "USER,ADMIN").
        // On accepte si l'utilisateur possede AU MOINS UN des roles demandes.
        for (String required : requiredRole.split(",")) {
            String normalized = required.trim().toUpperCase();
            if (!normalized.isEmpty() && userRoles.contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> extractRoles(Object value) {
        Set<String> roles = new HashSet<>();
        if (value == null) {
            return roles;
        }

        if (value instanceof String) {
            for (String role : ((String) value).split(",")) {
                addRole(roles, role);
            }
        } else if (value instanceof Collection<?>) {
            for (Object role : (Collection<?>) value) {
                if (role != null) {
                    addRole(roles, role.toString());
                }
            }
        } else if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Object role = Array.get(value, i);
                if (role != null) {
                    addRole(roles, role.toString());
                }
            }
        } else {
            addRole(roles, value.toString());
        }

        return roles;
    }

    private void addRole(Set<String> roles, String role) {
        if (role != null && !role.isBlank()) {
            roles.add(role.trim().toUpperCase());
        }
    }

    private void handleApi(ApiMethods apiMethod, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Method method = apiMethod.getMethod();
        if (!hasPermission(method, request)) {
            deny(request, response);
            return;
        }

        try {
            Object result = invokeControllerMethod(apiMethod.getController(), method, request);
            renderApiResult(result, apiMethod.isToJson(), response);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;

            // Client deconnecte (ClientAbortException chez Tomcat) ou reponse deja
            // envoyee : on ne peut plus ecrire d'erreur, on laisse Tomcat gerer.
            if (isClientAbort(cause) || response.isCommitted()) {
                throw e;
            }

            if (cause instanceof ServletException && ((ServletException) cause).getRootCause() == null) {
                throw (ServletException) cause;
            }

            retournerAPIError(e, response);
        }
    }

    /**
     * Detecte une deconnexion du client sans referencer directement une classe
     * propre a Tomcat (org.apache.catalina.connector.ClientAbortException).
     *
     * Cette classe ne fait pas partie de l'API Servlet (servlet-api.jar) : la
     * comparer par son nom permet de compiler le framework sans ajouter
     * catalina.jar au classpath, et donc de rester portable.
     */
    private boolean isClientAbort(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String className = current.getClass().getName();
            if ("org.apache.catalina.connector.ClientAbortException".equals(className)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void deny(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isAuthenticated(request)) {
            // Connecte mais role insuffisant.
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Role insuffisant");
        } else {
            // Pas connecte.
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentification requise");
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

    private void retournerAPIError(Exception e, HttpServletResponse response) {
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
        Method method = mvcMethod.getMethod();
        if (!hasPermission(method, request)) {
            deny(request, response);
            return;
        }

        Object result = invokeControllerMethod(mvcMethod.getControllerName(), method, request);

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
        String prefix = url;
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash >= 0) {
            prefix = url.substring(0, lastSlash);
        }
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

    private final ModelBinder modelBinder = new ModelBinder();

    private Object invokeControllerMethod(String controllerName, Method method, HttpServletRequest request)
            throws Exception {

        Class<?> controllerClass = Class.forName(controllerName);
        Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
        method.setAccessible(true);

        Parameter[] parametres = method.getParameters();

        Object[] args = new Object[parametres.length];

        Session session = null;

        for (int i = 0; i < parametres.length; i++) {
            Parameter parameter = parametres[i];
            Class<?> type = parameter.getType();

            if (ApplicationContext.class.isAssignableFrom(type)) {
                args[i] = ctx;

            } else if (isSession(type)) {

                session = getUpdatedSession(request);
                args[i] = session;

            } else if (isMapFile(type)) {
                args[i] = modelBinder.bindFichier(parameter.getName(), request);
            } else if (isObjet(type)) {
                args[i] = modelBinder.bind(type, request);
            } else {
                String value = request.getParameter(parameter.getName());
                args[i] = modelBinder.convert(value, type, parameter.getName());
            }
        }

        Object resultat = method.invoke(controllerInstance, args);

        if (session != null) {
            updateSession(request, session);
        }

        return resultat;
    }

    private boolean isObjet(Class<?> type) {
        return !type.isPrimitive()
                && !type.equals(String.class)
                && !type.equals(Integer.class)
                && !type.equals(Long.class)
                && !type.equals(Double.class)
                && !type.equals(Boolean.class)
                && !type.equals(Float.class)
                && !type.equals(Short.class)
                && !type.equals(Byte.class)
                && !type.isEnum()
                && !MapFile.class.equals(type)
                && !Session.class.equals(type)
                && !type.equals(ApplicationContext.class);
    }

    private boolean isMapFile(Class<?> type) {
        return type.equals(MapFile.class);
    }

    private boolean isSession(Class<?> type) {
        return type.equals(Session.class);
    }

    private Session getUpdatedSession(HttpServletRequest request) {
        Session session = (Session) getServletContext().getAttribute("Session");

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            return session;
        }
        Enumeration<String> attributs = httpSession.getAttributeNames();
        while (attributs.hasMoreElements()) {
            String nomAttribut = attributs.nextElement();
            session.set(nomAttribut, httpSession.getAttribute(nomAttribut));
        }
        return session;
    }

    private void updateSession(HttpServletRequest request, Session session) {
        Map<String, Object> attributs = session.getSessionAttributs();

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            if (attributs.isEmpty()) {
                return;
            }
            httpSession = request.getSession();
        }

        List<String> aSupprimer = new ArrayList<>();
        Enumeration<String> existants = httpSession.getAttributeNames();
        while (existants.hasMoreElements()) {
            String nom = existants.nextElement();
            if (!attributs.containsKey(nom)) {
                aSupprimer.add(nom);
            }
        }
        for (String nom : aSupprimer) {
            session.removeFromHttpSession(nom, httpSession);
        }

        for (Map.Entry<String, Object> entry : attributs.entrySet()) {
            httpSession.setAttribute(entry.getKey(), entry.getValue());
        }
    }
}