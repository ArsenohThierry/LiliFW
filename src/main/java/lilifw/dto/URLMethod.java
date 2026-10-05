package lilifw.dto;

import java.util.Objects;

public class URLMethod {
    private final String url;
    private final String method;

    public URLMethod(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URLMethod urlMethod = (URLMethod) o;
        return Objects.equals(url, urlMethod.url) && Objects.equals(method, urlMethod.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, method);
    }
}