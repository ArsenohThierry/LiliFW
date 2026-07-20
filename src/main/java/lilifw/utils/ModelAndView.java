package lilifw.utils;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private String view;
    private Map<String, Object> data = new HashMap<>();

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public void addObject(String name, Object value) {
        data.put(name, value);
    }

    public Map<String, Object> getData() {
        return data;
    }
}
