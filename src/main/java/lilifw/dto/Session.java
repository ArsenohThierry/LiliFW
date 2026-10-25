package lilifw.dto;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

public class Session {
    Map<String, Object> sessionAttributs;
    
    public Session() {
        sessionAttributs = new HashMap<>();
    }

    public void set(String key, Object value) {
        sessionAttributs.put(key, value);
    }

    public Object get(String key) {
        return sessionAttributs.get(key);
    }

    public void remove(String key) {
        sessionAttributs.remove(key);
    }

    public void removeFromHttpSession(String key, HttpSession session){
        session.removeAttribute(key);
    }

    public Map<String, Object> getSessionAttributs() {
        return sessionAttributs;
    }

    public void setSessionAttributs(Map<String, Object> sessionAttributs) {
        this.sessionAttributs = sessionAttributs;
    }

    public String[] getAllSessionAttributes() {
        return sessionAttributs.keySet().toArray(new String[0]);
    }
}
