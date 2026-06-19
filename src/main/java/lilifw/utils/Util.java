package lilifw.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class Util {
    // besoin de 2 fonctions

    // fonction 1 miparcourir ny classes reetran amina package 1 de mi retourne
    // liste des classes dans ce package
    public static List<String> getClassesForPackage(String packageName) {
        List<String> listClasses = new ArrayList<>();

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            String path = packageName.replace('.', '/');
            URL resource = classLoader.getResource(path);

            if (resource == null) {
                return listClasses;
            }

            File directory = new File(resource.toURI());

            if (!directory.exists()) {
                return listClasses;
            }

            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();

                    if (fileName.endsWith(".class")) {
                        String className = packageName + "."
                                + fileName.substring(0, fileName.length() - 6);

                        listClasses.add(className);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return listClasses;
    }

    // fonction 2 mandray anle liste de oe Inn le annotation ho verifiena :
    // (Controller), de oe niveau inn ( Controlleru , methode , attributs)
    @SuppressWarnings("unchecked")
    public static List<String> getAnnotatedClasses(
            String packageName, 
            String nomAnnotation,
            String niveauAnnotation) {

        nomAnnotation = "lilifw.annotation." + nomAnnotation;
        
        List<String> listeClasses = getClassesForPackage(packageName);

        List<String> listeClassesAnnotes = new ArrayList<>();

        try {
            Class<?> annotationClass = Class.forName(nomAnnotation);

            for (String nomClasse : listeClasses) {

                Class<?> clazz = Class.forName(nomClasse);

                boolean trouve = false;

                switch (niveauAnnotation.toLowerCase()) {

                    case "classe":
                    case "controller":
                        trouve = clazz.isAnnotationPresent(
                                (Class<? extends Annotation>) annotationClass);
                        break;

                    case "methode":
                        for (Method method : clazz.getDeclaredMethods()) {
                            if (method.isAnnotationPresent(
                                    (Class<? extends Annotation>) annotationClass)) {
                                trouve = true;
                                break;
                            }
                        }
                        break;

                    case "attribut":
                        for (Field field : clazz.getDeclaredFields()) {
                            if (field.isAnnotationPresent(
                                    (Class<? extends Annotation>) annotationClass)) {
                                trouve = true;
                                break;
                            }
                        }
                        break;
                }

                if (trouve) {
                    listeClassesAnnotes.add(nomClasse);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return listeClassesAnnotes;
    }
}
