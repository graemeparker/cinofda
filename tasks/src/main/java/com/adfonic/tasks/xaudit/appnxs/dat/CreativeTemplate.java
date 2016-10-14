package com.adfonic.tasks.xaudit.appnxs.dat;

/**
 * https://wiki.appnexus.com/display/adnexusdocumentation/Creative+Service#CreativeService-CreativeTemplate
 * 
 * @author mvanek
 * 
    ID  Format     Name
    1   url-html    Standard Banner
    2   url-js  Standard Banner
    3   flash   Standard Banner
    4   image   Standard Banner
    5   raw-js  Standard Banner
    6   raw-html    Standard Banner
    7   iframe-html Standard Banner
    8   url-xml In-Banner Video
    9   url-html    Popup
    10  url-js  Popup
    11  flash   Popup
    12  image   Popup
    13  raw-js  Popup
    14  raw-html    Popup
    15  iframe-html Popup
 */
public class CreativeTemplate {

    public static final CreativeTemplate IFRAME_HTML = new CreativeTemplate(7);

    private int id;

    public CreativeTemplate() {
        //default
    }

    public CreativeTemplate(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

}
