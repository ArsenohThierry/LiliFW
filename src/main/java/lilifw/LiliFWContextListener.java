package lilifw;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import lilifw.dto.UrlMappings;
import lilifw.utils.ControllerScanner;

public class LiliFWContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        String packageLocation = context.getInitParameter("package");

        try {
            UrlMappings mappings = new ControllerScanner().scan(packageLocation);
            context.setAttribute("urlToMethods", mappings.getUrlToMethods());
            context.setAttribute("urlToApiMethods", mappings.getUrlToApiMethods());
        } catch (Exception e) {
            throw new RuntimeException("Erreur au scan des controleurs: " + e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }
}