package lilifw.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;

import lilifw.annotation.Controller;
import lilifw.annotation.UrlMapping;

public class Util {
    // public static void scanAllAnnotedControllers(String
    // packageLocation,List<Class<?>> liste_controller) throws Exception{
    public static void scanAllAnnotedControllers(String packageLocation, Map<URLMethod, ControllerMethods> urlToMethods)
            throws Exception {

        packageLocation = packageLocation.replace(".", "/") + "/controllers";
        URL url = Thread.currentThread().getContextClassLoader().getResource(packageLocation);

        if (url == null) {
            System.out.println("No controllers package found at: " + packageLocation);
            return;
        }

        File dossier = new File(url.toURI());

        for (File file : dossier.listFiles()) {
            // recuperer tous les noms de classes
            if (file.isFile() && file.getName().endsWith(".class")) {
                // transformer en Nom de classes sans .class
                String className = packageLocation.replace("/", ".") + "." + file.getName().replace(".class", "");
                // transformer en Class et verifier si annote controller , si oui ajouter dans
                // la liste si non continue
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(Controller.class)) {
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(UrlMapping.class)) {
                            String urlMethod = method.getAnnotation(UrlMapping.class).value();
                            String reqMethod = method.getAnnotation(UrlMapping.class).method();
                            URLMethod reqUrlMethod = new URLMethod(urlMethod, reqMethod);
                            
                            if (urlToMethods.containsKey(reqUrlMethod)) {
                                ControllerMethods existing = urlToMethods.get(reqUrlMethod);
                                throw new Exception("Conflit d'URL : '" + urlMethod + "' est deja utilise par "
                                        + existing.getControllerName() + "." + existing.getMethode().getName()
                                        + " et par " + clazz.getName() + "." + method.getName());
                            }

                            urlToMethods.put(reqUrlMethod, new ControllerMethods(clazz.getName(), method));
                        }
                    }
                }
                else {
                    continue;
                }

            }
        }
    }
}