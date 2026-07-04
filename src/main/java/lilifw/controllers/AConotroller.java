package lilifw.controllers;

import lilifw.annotation.Controller;
import lilifw.annotation.UrlMapping;

@Controller
public class AConotroller {
    @UrlMapping("/test")
    public void test(){

    }

    @UrlMapping("/annote")
    public void nonAnnote(){

    }
}
