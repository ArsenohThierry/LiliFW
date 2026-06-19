package lilifw;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FormControllerServlet extends HttpServlet {
    static List<String> listAnnotesController = new ArrayList<>();
    String packageName;
    String nomAnnotation;
    String niveauAnnotation;
    
    public void init() throws ServletException {

        packageName = this.getInitParameter("package_controllers");
        nomAnnotation = this.getInitParameter("annotation");
        niveauAnnotation = "classe";

        if (packageName != null) {
            listAnnotesController = scanWithServletContext(packageName);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> scanWithServletContext(String packageName) {
        List<String> result = new ArrayList<>();
        String directoryPath = "/WEB-INF/classes/" + packageName.replace('.', '/');
        Set<String> paths = getServletContext().getResourcePaths(directoryPath);
        if (paths != null) {
            for (String path : paths) {
                if (path.endsWith(".class")) {
                    String fileName = path.substring(path.lastIndexOf('/') + 1);
                    String fqcn = packageName + "." + fileName.substring(0, fileName.length() - 6);
                    try {
                        Class<?> clazz = Class.forName(fqcn);
                        String annotationFqn = "lilifw.annotation." + nomAnnotation;
                        Class<? extends Annotation> annotationClass =
                            (Class<? extends Annotation>) Class.forName(annotationFqn);
                        boolean trouve = false;
                        switch (niveauAnnotation.toLowerCase()) {
                            case "classe":
                            case "controller":
                                trouve = clazz.isAnnotationPresent(annotationClass);
                                break;
                            case "methode":
                                for (Method method : clazz.getDeclaredMethods()) {
                                    if (method.isAnnotationPresent(annotationClass)) {
                                        trouve = true;
                                        break;
                                    }
                                }
                                break;
                            case "attribut":
                                for (Field field : clazz.getDeclaredFields()) {
                                    if (field.isAnnotationPresent(annotationClass)) {
                                        trouve = true;
                                        break;
                                    }
                                }
                                break;
                        }
                        if (trouve) {
                            result.add(fqcn);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    public static String processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = request.getRequestURI();
        return url;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("controllers", listAnnotesController);
        String url = processRequest(request, response);
        request.setAttribute("url", url);
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("controllers", listAnnotesController);
        String url = processRequest(request, response);
        request.setAttribute("url", url);
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

    public static List<String> getListAnnotesController() {
        return listAnnotesController;
    }
}