package lilifw;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import lilifw.dto.Session;
import lilifw.dto.UrlMappings;
import lilifw.utils.ControllerScanner;

public class LiliFWContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        String packageLocation = context.getInitParameter("package");
        
        // Pour gerer les authentifications : le framework lit si cette variable est stocke en session
        String authSessionParameter = context.getInitParameter("authSession");
        
        if (authSessionParameter != null) {
            context.setAttribute("authSessionParameter", authSessionParameter);
        }
        
        context.setAttribute("Session", new Session());
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