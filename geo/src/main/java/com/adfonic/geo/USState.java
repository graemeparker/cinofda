package com.adfonic.geo;

public enum USState implements Coordinates {
    AL("Alabama", "Montgomery", 32.3668052, -86.2999689),
    AK("Alaska", "Juneau", 58.3019444, -134.4197222),
    AZ("Arizona", "Phoenix", 33.4483771, -112.0740373),
    AR("Arkansas", "Little Rock", 34.7464809, -92.2895948),
    CA("California", "Sacramento", 38.5815719, -121.4943996),
    CO("Colorado", "Denver", 39.7391536, -104.9847034),
    CT("Connecticut", "Hartford", 41.7637111, -72.6850932),
    DC("District of Columbia", "Washington", 38.895108, -77.036347),
    DE("Delaware", "Dover", 39.158168, -75.5243682),
    FL("Florida", "Tallahassee", 30.4382559, -84.2807329),
    GA("Georgia", "Atlanta", 33.7489954, -84.3879824),
    HI("Hawaii", "Honolulu", 21.3069444, -157.8583333),
    ID("Idaho", "Boise", 43.612631, -116.211076),
    IL("Illinois", "Springfield", 39.7817213, -89.6501481),
    IN("Indiana", "Indianapolis", 39.7683765, -86.1580423),
    IA("Iowa", "Des Moines", 41.6005448, -93.6091064),
    KS("Kansas", "Topeka", 39.0558235, -95.6890185),
    KY("Kentucky", "Frankfort", 38.2009055, -84.8732835),
    LA("Louisiana", "Baton Rouge", 30.4507462, -91.154551),
    ME("Maine", "Augusta", 44.3106241, -69.7794897),
    MD("Maryland", "Annapolis", 38.9784453, -76.4921829),
    MA("Massachusetts", "Boston", 42.3584308, -71.0597732),
    MI("Michigan", "Lansing", 42.732535, -84.5555347),
    MN("Minnesota", "Saint Paul", 44.944167, -93.086075),
    MS("Mississippi", "Jackson", 32.2987573, -90.1848103),
    MO("Missouri", "Jefferson City", 38.5767017, -92.1735164),
    MT("Montana", "Helana", 46.5958056, -112.0270306),
    NE("Nebraska", "Lincoln", 40.806862, -96.681679),
    NV("Nevada", "Carson City", 39.1637984, -119.7674034),
    NH("New Hampshire", "Concord", 43.2081366, -71.5375718),
    NJ("New Jersey", "Trenton", 40.2170534, -74.7429384),
    NM("New Mexico", "Santa Fe", 35.6869752, -105.937799),
    NY("New York", "Albany", 42.6525793, -73.7562317),
    NC("North Carolina", "Raleigh", 35.772096, -78.6386145),
    ND("North Dakota", "Bismarck", 46.8083268, -100.7837392),
    OH("Ohio", "Columbus", 39.9611755, -82.9987942),
    OK("Oklahoma", "Oklahoma City", 35.4675602, -97.5164276),
    OR("Oregon", "Salem", 44.9428975, -123.0350963),
    PA("Pennsylvania", "Harrisburg", 40.2737002, -76.8844179),
    RI("Rhode Island", "Providence", 41.8239891, -71.4128343),
    SC("South Carolina", "Columbia", 34.0007104, -81.0348144),
    SD("South Dakota", "Pierre", 44.3683156, -100.3509665),
    TN("Tennessee", "Nashville", 36.1658899, -86.7844432),
    TX("Texas", "Austin", 30.267153, -97.7430608),
    UT("Utah", "Salt Lake City", 40.7607793, -111.8910474),
    VT("Vermont", "Montpelier", 44.2600593, -72.5753869),
    VA("Virginia", "Richmond", 37.542979, -77.469092),
    WA("Washington", "Olympia", 47.0378741, -122.9006951),
    WV("West Virginia", "Charleston", 38.3498195, -81.6326234),
    WI("Wisconsin", "Madison", 43.0730517, -89.4012302),
    WY("Wyoming", "Cheyenne", 41.1399814, -104.8202462),
    ;
    
    private String name;
    private String capitalCity;
    private double latitude;
    private double longitude;
        
    private USState(String name,
                    String capitalCity,
                    double latitude,
                    double longitude) {
        this.name = name;
        this.capitalCity = capitalCity;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public String getName() { 
        return name; 
    
    }
    public String getCapitalCity() { 
        return capitalCity; 
    }
    
    @Override
    public double getLatitude() { 
        return latitude; 
    }
    
    @Override
    public double getLongitude() { 
        return longitude; 
    }
    
    public static USState byName(String name) {
        for (USState value : values()) {
            if (value.getName().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }
}
