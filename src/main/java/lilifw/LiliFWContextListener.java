package lilifw;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import lilifw.utils.ControllerMethods;
import lilifw.utils.URLMethod;
import lilifw.utils.Util;

public class LiliFWContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        String packageLocation = context.getInitParameter("package");

        Map<URLMethod, ControllerMethods> urlToMethods = new HashMap<>();

        try {
            Util.scanAllAnnotedControllers(packageLocation, urlToMethods);
            context.setAttribute("urlToMethods", urlToMethods);
        } catch (Exception e) {
            throw new RuntimeException("Erreur au scan des controleurs: " + e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }
}
