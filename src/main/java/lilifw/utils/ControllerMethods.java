package lilifw.utils;

import java.lang.reflect.Method;

public class ControllerMethods {
    private String controllerName;
    private Method method;

    public ControllerMethods(String controllerName, Method method) {
        this.controllerName = controllerName;
        this.method = method;
    }

    public String getControllerName() {
        return controllerName;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}