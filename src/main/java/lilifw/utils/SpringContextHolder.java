package lilifw.utils;

import org.springframework.context.ApplicationContext;

public class SpringContextHolder {

    private static ApplicationContext ctx;

    public static void set(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    public static ApplicationContext get() {
        return ctx;
    }

    public static <T> T getBean(Class<T> clazz) {
        return ctx.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return ctx.getBean(name, clazz);
    }
}
