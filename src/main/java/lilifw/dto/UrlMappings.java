package lilifw.dto;

import java.util.HashMap;
import java.util.Map;

public class UrlMappings {

    private final Map<URLMethod, ControllerMethods> urlToMethods = new HashMap<>();
    private final Map<URLMethod, ApiMethods> urlToApiMethods = new HashMap<>();

    public Map<URLMethod, ControllerMethods> getUrlToMethods() {
        return urlToMethods;
    }

    public Map<URLMethod, ApiMethods> getUrlToApiMethods() {
        return urlToApiMethods;
    }
}