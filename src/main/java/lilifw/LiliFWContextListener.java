package lilifw;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import lilifw.utils.ControllerMethods;
import lilifw.utils.SpringContextHolder;
import lilifw.utils.URLMethod;
import lilifw.utils.Util;

public class LiliFWContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        String packageLocation = context.getInitParameter("package");

        try {
            ApplicationContext springCtx = new ClassPathXmlApplicationContext("applicationContext.xml");
            SpringContextHolder.set(springCtx);
        } catch (Exception e) {
            throw new RuntimeException("Erreur au chargement de Spring: " + e.getMessage(), e);
        }

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
