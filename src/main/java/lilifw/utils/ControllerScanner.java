package lilifw.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lilifw.annotation.Controller;
import lilifw.annotation.UrlMapping;
import lilifw.annotation.WebAPI;
import lilifw.dto.ApiMethods;
import lilifw.dto.ControllerMethods;
import lilifw.dto.URLMethod;
import lilifw.dto.UrlMappings;

public class ControllerScanner {

    private static final String CONTROLLERS_SUFFIX = "/controllers";
    private static final String API_HTTP_METHOD = "GET";

    public UrlMappings scan(String packageLocation) throws Exception {
        UrlMappings mappings = new UrlMappings();
        for (Class<?> clazz : findControllerClasses(packageLocation)) {
            registerMappings(clazz, mappings);
        }
        return mappings;
    }

    private List<Class<?>> findControllerClasses(String packageLocation) throws Exception {
        List<Class<?>> controllerClasses = new ArrayList<>();
        String resourcePath = packageLocation.replace(".", "/") + CONTROLLERS_SUFFIX;
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourcePath);

        if (url == null) {
            System.out.println("Pas de Controller dans : " + resourcePath);
            return controllerClasses;
        }

        File[] fichiers = new File(url.toURI()).listFiles();
        if (fichiers == null) {
            return controllerClasses;
        }
        Arrays.sort(fichiers);

        for (File file : fichiers) {
            if (!file.isFile() || !file.getName().endsWith(".class")) {
                continue;
            }
            String className = resourcePath.replace("/", ".") + "." + file.getName().replace(".class", "");
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(Controller.class)) {
                controllerClasses.add(clazz);
            }
        }
        return controllerClasses;
    }

    private void registerMappings(Class<?> clazz, UrlMappings mappings) throws Exception {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(UrlMapping.class)) {
                registerUrlMapping(clazz, method, mappings);
            } else if (method.isAnnotationPresent(WebAPI.class)) {
                enregistrerApiMethod(clazz, method, mappings);
            }
        }
    }

    private void registerUrlMapping(Class<?> clazz, Method method, UrlMappings mappings) throws Exception {
        String url = method.getAnnotation(UrlMapping.class).value();
        String httpMethod = method.getAnnotation(UrlMapping.class).method();
        URLMethod urlMethod = new URLMethod(url, httpMethod);

        assertNoConflict(urlMethod, clazz, method, mappings);
        mappings.getUrlToMethods().put(urlMethod, new ControllerMethods(clazz.getName(), method));
    }

    private void enregistrerApiMethod(Class<?> clazz, Method method, UrlMappings mappings) throws Exception {
        String url = method.getAnnotation(WebAPI.class).api();
        URLMethod urlMethod = new URLMethod(url, API_HTTP_METHOD);
        boolean toJson = method.getAnnotation(WebAPI.class).toJSON();

        assertNoConflict(urlMethod, clazz, method, mappings);
        mappings.getUrlToApiMethods().put(urlMethod, new ApiMethods(clazz.getName(), method, toJson));
    }

    private void assertNoConflict(URLMethod urlMethod, Class<?> clazz, Method method, UrlMappings mappings)
            throws Exception {
        ControllerMethods existingMvc = mappings.getUrlToMethods().get(urlMethod);
        if (existingMvc != null) {
            throw new Exception("Conflit d'URL : '" + urlMethod.getUrl() + "' est deja utilise par "
                    + existingMvc.getControllerName() + "." + existingMvc.getMethod().getName()
                    + " et par " + clazz.getName() + "." + method.getName());
        }

        ApiMethods existingApi = mappings.getUrlToApiMethods().get(urlMethod);
        if (existingApi != null) {
            throw new Exception("Conflit d'URL API : '" + urlMethod.getUrl() + "' est deja utilisee par "
                    + existingApi.getController() + "." + existingApi.getMethod().getName()
                    + " et par " + clazz.getName() + "." + method.getName());
        }
    }
}
