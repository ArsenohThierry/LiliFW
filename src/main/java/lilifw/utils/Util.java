package lilifw.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;

import lilifw.annotation.Controller;
import lilifw.annotation.UrlMapping;
import lilifw.annotation.WebAPI;

public class Util {

    public static void scanAllAnnotedControllers(String packageLocation, Map<URLMethod, ControllerMethods> urlToMethods,
            Map<URLMethod, ApiMethods> urlToApiMethods)
            throws Exception {

        packageLocation = packageLocation.replace(".", "/") + "/controllers";
        URL url = Thread.currentThread().getContextClassLoader().getResource(packageLocation);

        if (url == null) {
            System.out.println("Pas de Controller dans : " + packageLocation);
            return;
        }

        File dossier = new File(url.toURI());
        File[] fichiers = dossier.listFiles();
        if (fichiers == null) {
            return;
        }

        for (File file : fichiers) {
            if (file.isFile() && file.getName().endsWith(".class")) {
                String className = packageLocation.replace("/", ".") + "." + file.getName().replace(".class", "");
                Class<?> clazz = Class.forName(className);

                if (!clazz.isAnnotationPresent(Controller.class)) {
                    continue;
                }

                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(UrlMapping.class)) {
                        String urlMethod = method.getAnnotation(UrlMapping.class).value();
                        String reqMethod = method.getAnnotation(UrlMapping.class).method();
                        URLMethod reqUrlMethod = new URLMethod(urlMethod, reqMethod);

                        if (urlToMethods.containsKey(reqUrlMethod)) {
                            ControllerMethods existing = urlToMethods.get(reqUrlMethod);
                            throw new Exception("Conflit d'URL : '" + urlMethod + "' est deja utilise par "
                                    + existing.getControllerName() + "." + existing.getMethod().getName()
                                    + " et par " + clazz.getName() + "." + method.getName());
                        }

                        urlToMethods.put(reqUrlMethod, new ControllerMethods(clazz.getName(), method));
                    } else if (method.isAnnotationPresent(WebAPI.class)) {
                        String urlAPI = method.getAnnotation(WebAPI.class).api();
                        URLMethod reqApiMethod = new URLMethod(urlAPI, "GET");
                        boolean toJson = method.getAnnotation(WebAPI.class).toJSON();

                        if (urlToApiMethods.containsKey(reqApiMethod)) {
                            ApiMethods existing = urlToApiMethods.get(reqApiMethod);
                            throw new Exception("Conflit d'URL API : '" + urlAPI + "' est deja utilisee par "
                                    + existing.getController() + "." + existing.getMethod().getName()
                                    + " et par " + clazz.getName() + "." + method.getName());
                        }

                        for (URLMethod existingKey : urlToMethods.keySet()) {
                            if (existingKey.getUrl().equals(urlAPI)) {
                                ControllerMethods existing = urlToMethods.get(existingKey);
                                throw new Exception("Conflit d'URL : '" + urlAPI + "' est deja utilisee par "
                                        + existing.getControllerName() + "." + existing.getMethod().getName()
                                        + " et par " + clazz.getName() + "." + method.getName());
                            }
                        }

                        urlToApiMethods.put(reqApiMethod, new ApiMethods(className, method, toJson));
                    }
                }
            }
        }
    }
}