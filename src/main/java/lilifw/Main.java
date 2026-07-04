package lilifw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lilifw.utils.ControllerMethods;
import lilifw.utils.Util;

public class Main {
    public static void main(String[] args) {
        String url = "lilifw";

        Map<String,ControllerMethods> contMethods = new HashMap<>();
        
        try {
            Util.scanAllAnnotedControllers(url, contMethods);

            for (String string : contMethods.keySet()) {
                System.out.println("url: " + string + " ; ");
                contMethods.get(string).print();
                
            }
        } catch (Exception e) {
            
            e.printStackTrace();
        }
    }
}
