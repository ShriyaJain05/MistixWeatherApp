package weather;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1


public class WeatherAPI {
    public static ArrayList<Period> getForecast(String region, int gridx, int gridy) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.weather.gov/gridpoints/"+region+"/"+String.valueOf(gridx)+","+String.valueOf(gridy)+"/forecast"))
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Root r = getObject(response.body());
        if(r == null){
            System.err.println("Failed to parse JSon");
            return null;
        }
        return r.properties.periods;
    }
    public static Root getObject(String json){
        ObjectMapper om = new ObjectMapper();
        Root toRet = null;
        try {
            toRet = om.readValue(json, Root.class);
            ArrayList<Period> p = toRet.properties.periods;

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return toRet;

    }
    
    ///added this
    public static ArrayList<Period> getHourlyForecast(String region, int gridx, int gridy) {
        // Target the hourly end
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.weather.gov/gridpoints/" + region + "/" + gridx + "," + gridy + "/forecast/hourly"))
                .header("User-Agent", "MistixApp") // NWS requirement [cite: 15]
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            
            // Use our new safe parsing method instead of the original getObject
            Root r = getHourlyObject(response.body());
            if (r != null && r.properties != null) {
                return r.properties.periods;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Root getHourlyObject(String json) {
        ObjectMapper om = new ObjectMapper();
        om.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        try {
            return om.readValue(json, Root.class);
        } catch (Exception e) {
            System.err.println("Hourly Parse Error: " + e.getMessage());
            return null;
        }
    }
}



