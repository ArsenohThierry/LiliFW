package lilifw.dto;

import java.lang.reflect.Method;

public class ApiMethods {
    private String controller;
    private Method method;
    private boolean toJson;

    public ApiMethods(String controller, Method method, boolean toJson) {
        this.controller = controller;
        this.method = method;
        this.toJson = toJson;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public boolean isToJson() {
        return toJson;
    }

    public void setToJson(boolean toJson) {
        this.toJson = toJson;
    }
}